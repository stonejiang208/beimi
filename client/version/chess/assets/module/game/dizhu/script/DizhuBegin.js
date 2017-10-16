var beiMiCommon = require("BeiMiCommon");
cc.Class({
    extends: beiMiCommon,

    properties: {
        // foo: {
        //    default: null,      // The default value will be used only when the component attaching
        //                           to a node for the first time
        //    url: cc.Texture2D,  // optional, default is typeof default
        //    serializable: true, // optional, default is true
        //    visible: true,      // optional, default is true
        //    displayName: 'Foo', // optional
        //    readonly: false,    // optional, default is false
        // },
        // ...
        gamebtn: {
            default: null,
            type: cc.Node
        },
        poker: {
            default: null,
            type: cc.Node
        },
        lastCardsPanel: {   //底牌
            default: null,
            type: cc.Node
        },
        waitting: {
            default: null,
            type: cc.Prefab
        },
        summary_win:{
            default:null ,
            type : cc.Prefab
        },
        summary:{
            default:null ,
            type : cc.Prefab
        }
    },

    // use this for initialization
    onLoad: function () {
        this.routes = {} ;
        this.player = new Array() ;     //存放玩家数据
        this.pokercards = new Array();
        this.lastcards = new Array();
        this.lastCardsPanel.active = false ;

    },
    begin:function(){
        this.gamebtn.active = false ;
        this.initgame(false);
        this.waittimer = cc.instantiate(this.waitting);
        this.waittimer.parent = this.root();

        let timer = this.waittimer.getComponent("BeiMiTimer");
        if(timer){
            timer.init("正在匹配玩家" , 5 , this.waittimer);
        }
    },
    opendeal:function(){
        this.gamebtn.active = false ;
        this.initgame(false);
        this.waittimer = cc.instantiate(this.waitting);
        this.waittimer.parent = this.root();

        let timer = this.waittimer.getComponent("BeiMiTimer");
        if(timer){
            timer.init("正在匹配玩家" , 5 , this.waittimer);
        }
    },
    initgame:function(opendeal){
        let self = this ;

        if(this.ready()) {
            let socket = this.socket();

            var param = {
                token:cc.beimi.authorization,
                playway:cc.beimi.playway,
                orgi:cc.beimi.user.orgi
            } ;
            this.game = this.getCommon("DizhuDataBind");
            socket.emit("joinroom" ,JSON.stringify(param)) ;


            this.map("joinroom" , this.joinroom_event) ;          //加入房价
            this.map("players" , this.players_event) ;            //接受玩家列表
            this.map("catch" , this.catch_event) ;            //接受玩家列表
            this.map("catchresult" , this.catchresult_event) ;            //接受玩家列表
            this.map("lasthands" , this.lasthands_event) ;            //接受玩家列表
            this.map("takecards" , this.takecards_event) ;            //接受玩家列表
            this.map("play" , this.play_event) ;            //接受玩家列表
            this.map("allcards" , this.allcards_event) ;                //我出的牌

            socket.on("command" , function(result){
                var data = self.parse(result) ;
                self.route(data.command)(data , self);
            });
            /**
             * 接受传送的 玩家列表（含AI）
             */
            socket.on("players" , function(result){
                var data = self.parse(result) ;
                /**
                 * 处理 Players
                 */
                self.route("players")(data, self);
            });
        }

    },
    /**
     * 新创建牌局，首个玩家加入，进入等待状态，等待其他玩家加入，服务端会推送 players数据
     * @param data
     * @param context
     */
    joinroom_event:function(data , context){
        //显示 匹配中，并计时间，超过设定的倒计时时间即AI加入，根据当前的 玩家数量 匹配机器人
        if(data.id && data.id == cc.beimi.user.id){
            //本人，开始计时
            //console.log("本人") ;
            //self.player[0] = data ;
        }else{
            //其他玩家加入，初始化
            var inroom = false ;
            for(var i = 0 ; i < context.player.length ; i++){
                var player = context.player[i].getComponent("PlayerRender") ;
                if(player.userid == data.id){
                    inroom = true ;
                }
            }
            if(inroom == false){
                context.newplayer(context.player.length , self , data) ;
            }
        }
    },
    /**
     * 接收到服务端的 推送的 玩家数据，根据玩家数据 恢复牌局
     * @param data
     * @param context
     */
    players_event:function(data,context){
        if(data.length > 1){
            var inx = 0 ;
            for(i = 0 ; i<data.length ; i++){
                var player = data[i] ;
                var inroom = false ;
                for(var j = 0 ; j < context.player.length ; j++){
                    if(context.player[j].id == player.id){
                        inroom = true ;
                    }
                }
                if(inroom == false && player.id !== cc.beimi.user.id){
                    context.newplayer(context.player.length , context , player) ;
                }
            }
        }
    },
    /**
     * 接收到服务端的 推送的 玩家数据，根据玩家数据 恢复牌局
     * @param data
     * @param context
     */
    catch_event:function(data,context){
        if(data.userid == cc.beimi.user.id){    //该我抢
            context.game.catchtimer(15);
        }else{                              //该别人抢
            for(var inx =0 ; inx<context.player.length ; inx++){
                var render = context.player[inx].getComponent("PlayerRender") ;
                if(render.userid && render.userid == data.userid){
                    render.catchtimer(15);
                    break ;
                }
            }
        }
    },
    /**
     * 接收到服务端的 推送的 玩家数据，根据玩家数据 恢复牌局
     * @param data
     * @param context
     */
    catchresult_event:function(data,context){
        if(data.userid == cc.beimi.user.id){    //该我抢
            context.game.catchresult(data);
        }else{                              //该别人抢
            setTimeout(function(){
                context.getPlayer(data.userid).catchresult(data);
            },1500) ;
        }
    },
    /**
     * 接收到服务端的 推送的 玩家数据，根据玩家数据 恢复牌局
     * @param data
     * @param context
     */
    lasthands_event:function(data,context){
        var lasthands = context.decode(data.lasthands);
        /**
         * 底牌 ， 顶部的 三张底牌显示区域
         */
        for(var i=0 ; i<context.lastcards.length ; i++){
            var last = context.lastcards[i].getComponent("BeiMiCard") ;
            last.setCard(lasthands[i]);
            last.order();
        }
        /**
         * 当前玩家的 底牌处理
         */
        if(data.userid == cc.beimi.user.id) {
            context.game.lasthands(context , context.game , data ) ;
            /**
             * 隐藏 其他玩家的 抢地主/不抢地主的 提示信息
             */
            for(var inx =0 ; inx<context.player.length ; inx++){
                var render = context.player[inx].getComponent("PlayerRender") ;
                render.hideresult();
            }

            for(var i=0 ; i<lasthands.length ; i++){
                let pc = context.playcards(context.game , context ,2 * 300 + (6 + i) * 50-300, lasthands[i]) ;
                var beiMiCard = pc.getComponent("BeiMiCard") ;
                beiMiCard.order();
                context.registerProxy(pc);
            }
            context.game.playtimer(context.game,25 , true);
        }else{
            for(var inx =0 ; inx<context.player.length ; inx++){
                var render = context.player[inx].getComponent("PlayerRender") ;
                render.lasthands(context,context.game,data);
            }
            context.getPlayer(data.nextplayer).lasthands(context,context.game,data);
            context.getPlayer(data.nextplayer).playtimer(context.game , 25);
        }
        for(var inx =0 ; inx<context.pokercards.length ; inx++){
            var pc = context.pokercards[inx] ;
            pc.zIndex = 54 - pc.card ;
        }
    },
    /**
     * 接收到服务端的 推送的 玩家数据，根据玩家数据 恢复牌局
     * @param data
     * @param context
     */
    takecards_event:function(data,context){
        if(data.allow == true) {
            var lastcards ;
            if(data.donot == false){
                lastcards = context.decode(data.cards);        //解析牌型
            }
            if (data.userid == cc.beimi.user.id) {
                context.game.lasttakecards(context.game, context, data.cardsnum, lastcards , data);
            } else {
                context.getPlayer(data.userid).lasttakecards(context.game, context, data.cardsnum, lastcards , data);
            }
            context.game.selectedcards.splice(0 ,context.game.selectedcards.length );//清空
            if (data.nextplayer == cc.beimi.user.id) {
                context.game.playtimer(context.game, 25 , data.automic);
            } else {
                context.getPlayer(data.nextplayer).playtimer(context.game, 25);
            }
        }else{//出牌不符合规则，需要进行提示
            context.game.notallow.active = true ;
            setTimeout(function(){
                context.game.notallow.active = false ;
            } , 2000);
        }
    },
    /**
     * 接收到服务端的 推送的 玩家数据，根据玩家数据 恢复牌局
     * @param data
     * @param context
     */
    play_event:function(data,context){
        var mycards = context.decode(data.player.cards);
        if(context.waittimer){
            let timer = context.waittimer.getComponent("BeiMiTimer");
            if(timer){
                timer.stop(context.waittimer) ;
            }
        }

        let center = context.game.pokerpool.get();
        let left = context.game.pokerpool.get(),right = context.game.pokerpool.get();
        center.parent = context.root() ;
        left.parent = context.root() ;
        right.parent = context.root() ;
        center.setPosition(0,200);
        left.setPosition(0,200);
        right.setPosition(0,200);

        let finished = cc.callFunc(function (target , data) {
            if(data.game){
                data.game.pokerpool.put(data.current) ;
                data.game.pokerpool.put(data.left);
                data.game.pokerpool.put(data.right);

                /**
                 * 赋值，解码牌面
                 */
                for(var i=0 ; i<data.self.pokercards.length ; i++){
                    var pokencard = data.self.pokercards[i];
                    pokencard.getComponent("BeiMiCard").order();
                }

                data.self.lastCardsPanel.active = true ;

            }
        }, this , {game : context.game  , self: context, left :left , right : right , current : center});

        context.doLastCards(context.game , context , 3 , 0);

        /**
         *  发牌动作，每次6张，本人保留总计17张，其他人发完即回收
         */
        setTimeout(function() {
            context.dealing(context.game , 6 , context , 0 , left , right , mycards) ;
            setTimeout(function(){
                context.dealing(context.game , 6 , context , 1, left , right , mycards) ;
                setTimeout(function(){
                    context.dealing(context.game , 5 , context , 2, left , right , mycards , finished) ;
                    context.reordering(context);
                },500) ;
            },500) ;
        }, 0);
    },
    /**
     * 打完牌，进入结算界面，结算界面流程：
     * 1、提示你赢/输了
     * 2、1秒后所有玩家的牌翻出来，显示剩余的牌
     * 3、2秒后显示结算界面
     * 4、玩家点选明牌开始还是继续游戏
     * @param data
     * @param context
     */
    allcards_event:function(data , context){
        //结算界面，
        for(var i=0 ; i<data.players.length ; i++){
            var player = data.players[i] ;
            if(player.playuser != cc.beimi.user.id){
                var cards = context.decode(player.cards);        //解析牌型
                var temp = context.getPlayer(player.playuser) ;
                for(var inx = 0 ; inx < cards.length ; inx++) {
                    temp.lasttakecards(context.game, context, cards.length, cards, data);
                }
            }
        }
        setTimeout(function(){
            let temp = cc.instantiate(context.summary) ;
            temp.parent = context.root() ;
        } , 2000);
    },
    getPlayer:function(userid){
        var tempRender;
        for(var inx =0 ; inx<this.player.length ; inx++){
            var render = this.player[inx].getComponent("PlayerRender") ;
            if(render.userid && render.userid == userid){
                tempRender = render ; break ;
            }
        }
        return tempRender ;
    },
    dealing:function(game , num , self , times , left , right , cards , finished){
        /**
         * 处理当前玩家的 牌， 发牌 ，  17张牌， 分三次动作处理完成
         */
        for(var i=0 ; i<num ; i++){
            var myCards ;
            if(finished == null){
                myCards = self.playcards(game , self ,times * 300 + i * 50-300, cards[times * 6 + i] ) ;
            }else{
                myCards = self.current(game , self ,times * 300 + i * 50-300, cards[times * 6 + i] , finished) ;
            }
            this.registerProxy(myCards);
        }
        self.otherplayer(left  , 0 , num ,game , self) ;
        self.otherplayer(right , 1 , num ,game , self) ;

    },
    otherplayer:function(currpoker , inx, num ,game , self){
        if(inx == 0){
            let seq = cc.sequence(
                cc.spawn(cc.moveTo(0.2, -350, 50) , cc.scaleTo(0.2, 0.3, 0.3)) , cc.moveTo(0 , 0 , 200) , cc.scaleTo(0, 1, 1)
            );
            currpoker.runAction(seq);
        }else{
            let seq = cc.sequence(
                cc.spawn(cc.moveTo(0.2, 350, 50) , cc.scaleTo(0.2, 0.3, 0.3)) , cc.moveTo(0 , 0 , 200) , cc.scaleTo(0, 1, 1)
            );
            currpoker.runAction(seq);
        }
        //currpoker.setScale(1);
        var render = self.player[inx].getComponent("PlayerRender") ;
        for(var i=0 ; i<num ; i++){
            render.countcards(1);
        }
    },
    playcards:function(game , self , posx , card){
        let currpoker = game.pokerpool.get() ;
        var beiMiCard = currpoker.getComponent("BeiMiCard") ;
        beiMiCard.setCard(card) ;

        currpoker.card = card ;


        currpoker.parent = self.poker ;
        currpoker.setPosition(0,200);

        self.pokercards[self.pokercards.length] = currpoker ;
        let action = cc.moveTo(0.2, posx, -180) ;

        currpoker.setScale(1,1);

        currpoker.runAction(action);
        return currpoker;
    },
    doLastCards:function(game , self , num , card){//发三张底牌
        for(var i=0 ; i<num ; i++){
            var width = i * 80 - 80;
            let currpoker = game.pokerpool.get() ;
            currpoker.getComponent("BeiMiCard").setCard(card) ;
            currpoker.card = card ;
            currpoker.parent = this.lastCardsPanel;
            currpoker.setPosition(width , 0);
            currpoker.setScale(0.5 , 0.5);

            self.lastcards[self.lastcards.length] = currpoker ;
        }
    },
    registerProxy:function(myCard){
        if(myCard){
            var beiMiCard = myCard.getComponent("BeiMiCard") ;
            beiMiCard.proxy(this.game);
        }
    },
    current:function(game , self , posx , card , func){
        let currpoker = game.pokerpool.get() ;
        var beiMiCard = currpoker.getComponent("BeiMiCard") ;
        beiMiCard.setCard(card) ;
        currpoker.card = card ;
        currpoker.parent = self.root() ;
        currpoker.setPosition(0,200);

        currpoker.setScale(1,1);



        self.pokercards[self.pokercards.length] = currpoker ;
        let seq = cc.sequence(cc.moveTo(0.2, posx, -180) , func);

        currpoker.runAction(seq);
        return currpoker;
    },
    reordering:function(self){
        for(var i=0 ; i<self.pokercards.length ; i++){
            self.pokercards[i].parent = self.poker ;
        }
    },
    newplayer:function(inx , self , data){
        var isRight = false ;
        if(self.player.length == 1){
            isRight = true ;
        }
        var pos = cc.v2(520, 100) ;
        if(isRight == false){
            pos = cc.v2(-520,100) ;
        }
        let game = self.getCommon("DizhuDataBind");
        if(game && game.playerspool.size() > 0){
            self.player[inx] = game.playerspool.get() ;
            self.player[inx].parent = self.root() ;
            self.player[inx].setPosition(pos);
            var render = self.player[inx].getComponent("PlayerRender") ;
            render.initplayer(data , isRight);
        }
    },
    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
    /**
     * 不抢/叫地主
     */
    givup:function(){
        if(this.ready()){
            let socket = this.socket();
            socket.emit("giveup");
        }
    },
    /**
     * 抢/叫地主触发事件
     */
    docatch:function(){
        if(this.ready()){
            let socket = this.socket();
            socket.emit("docatch");
        }
    },
    /**
     * 出牌
     */
    doPlayCards:function(){
        if(this.ready()){
            let socket = this.socket();
            this.game.selectedcards.splice(0 , this.game.selectedcards.length) ;
            for(var i=0 ; i<this.pokercards.length ; i++){
                var card = this.pokercards[i] ;
                var temp = card.getComponent("BeiMiCard");
                if(temp.selected == true){
                    this.game.selectedcards.push(temp.card) ;
                }
                temp.unselected();
            }
            socket.emit("doplaycards" , this.game.selectedcards.join());
        }
    },
    /**
     * 不出牌
     */
    noCards:function(){
        if(this.ready()){
            let socket = this.socket();
            socket.emit("nocards");
        }
    }
});
