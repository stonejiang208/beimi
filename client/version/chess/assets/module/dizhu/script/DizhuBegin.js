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
        }
    },

    // use this for initialization
    onLoad: function () {
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
        this.game = this.getCommon("DizhuDataBind");
        this.socket = window.io.connect(cc.beimi.http.wsURL + '/bm/game');
        this.socket.on("connect" , function(){
            var param = {
                token:cc.beimi.authorization,
                playway:'402888815e21d735015e21d995680000',
                orgi:cc.beimi.user.orgi
            } ;
            self.socket.emit("joinroom" ,JSON.stringify(param)) ;
        });
        /**
         * opendeal:明牌
         * playway:传入的玩法参数
         * 连接服务器，进入ROOM，开始等待其他玩家或AI加入 , 发送 创建ROOM的 HTTP请求，创建 ROOM ，ROOM创建成功后开始建立 SocketIO链接
         */
        if(cc.beimi && cc.beimi.authorization && cc.beimi.user){

            this.socket.on("joinroom" , function(result){
                var data = self.parse(result) ;

                //显示 匹配中，并计时间，超过设定的倒计时时间即AI加入，根据当前的 玩家数量 匹配机器人
                if(data.id && data.id == cc.beimi.user.id){
                    //本人，开始计时
                    //console.log("本人") ;
                    //self.player[0] = data ;
                }else{
                    //其他玩家加入，初始化
                    var inroom = false ;
                    for(var i = 0 ; i < self.player.length ; i++){
                        var player = self.player[i].getComponent("PlayerRender") ;
                        if(player.userid == data.id){
                            inroom = true ;
                        }
                    }
                    if(inroom == false){
                        self.newplayer(self.player.length , self , data) ;
                    }
                }
            });
            this.socket.on("players" , function(result){
                var data = self.parse(result);
                if(data.length > 1){
                    var inx = 0 ;
                    for(i = 0 ; i<data.length ; i++){
                        var player = data[i] ;
                        var inroom = false ;
                        for(var j = 0 ; j < self.player.length ; j++){
                            if(self.player[j].id == player.id){
                                inroom = true ;
                            }
                        }
                        if(inroom == false && player.id !== cc.beimi.user.id){
                            self.newplayer(self.player.length , self , player) ;
                        }
                    }
                }
            });
            /**
             * 开始抢地主
             */
            this.socket.on("catch" , function(result){
                var data = self.parse(result);
                if(data.userid == cc.beimi.user.id){    //该我抢
                    self.game.catchtimer(15);
                }else{                              //该别人抢
                    for(var inx =0 ; inx<self.player.length ; inx++){
                        var render = self.player[inx].getComponent("PlayerRender") ;
                        if(render.userid && render.userid == data.userid){
                            render.catchtimer(15);
                            break ;
                        }
                    }
                }
            });

            /**
             * 通知抢地主结果
             */
            this.socket.on("catchresult" , function(result){
                var data = self.parse(result);
                if(data.userid == cc.beimi.user.id){    //该我抢
                    self.game.catchresult(data);
                }else{                              //该别人抢
                    setTimeout(function(){
                        self.getPlayer(data.userid).catchresult(data);
                    },1500) ;
                }
            });

            /**
             * 底牌
             */
            this.socket.on("lasthands" , function(result){
                var data = self.parse(result);
                var lasthands = self.decode(data.lasthands);
                /**
                 * 底牌 ， 顶部的 三张底牌显示区域
                 */
                for(var i=0 ; i<self.lastcards.length ; i++){
                    var last = self.lastcards[i].getComponent("BeiMiCard") ;
                    last.setCard(lasthands[i]);
                    last.order();
                }
                /**
                 * 当前玩家的 底牌处理
                 */
                if(data.userid == cc.beimi.user.id) {
                    self.game.lasthands(self , self.game , data ) ;
                    /**
                     * 隐藏 其他玩家的 抢地主/不抢地主的 提示信息
                     */
                    for(var inx =0 ; inx<self.player.length ; inx++){
                        var render = self.player[inx].getComponent("PlayerRender") ;
                        render.hideresult();
                    }

                    for(var i=0 ; i<lasthands.length ; i++){
                        let pc = self.playcards(self.game , self ,2 * 300 + (6 + i) * 50-300, lasthands[i]) ;
                        var beiMiCard = pc.getComponent("BeiMiCard") ;
                        beiMiCard.order();
                        self.registerProxy(pc);
                    }
                    self.game.playtimer(self.game,25);
                }else{
                    for(var inx =0 ; inx<self.player.length ; inx++){
                        var render = self.player[inx].getComponent("PlayerRender") ;
                        render.lasthands(self,self.game,data);
                    }
                    self.getPlayer(data.nextplayer).lasthands(self,self.game,data);
                    self.getPlayer(data.nextplayer).playtimer(self.game , 25);
                }
                for(var inx =0 ; inx<self.pokercards.length ; inx++){
                    var pc = self.pokercards[inx] ;
                    pc.zIndex = 54 - pc.card ;
                }
            });

            /**
             * 出牌
             */
            this.socket.on("takecards" , function(result){
                var data = self.parse(result);
                if(data.allow == true) {
                    var lastcards ;
                    if(data.donot == false){
                        lastcards = self.decode(data.cards);        //解析牌型
                    }
                    if (data.userid == cc.beimi.user.id) {
                        self.game.lasttakecards(self.game, self, data.cardsnum, lastcards , data);
                    } else {
                        self.getPlayer(data.userid).lasttakecards(self.game, self, data.cardsnum, lastcards , data);
                    }
                    self.game.selectedcards.splice(0 ,self.game.selectedcards.length );//清空
                    if (data.nextplayer == cc.beimi.user.id) {
                        self.game.playtimer(self.game, 25);
                    } else {
                        self.getPlayer(data.nextplayer).playtimer(self.game, 25);
                    }
                }else{//出牌不符合规则，需要进行提示
                    self.game.notallow.active = true ;
                    setTimeout(function(){
                        self.game.notallow.active = false ;
                    } , 2000);
                }

            });

            this.socket.on("play" , function(result){
                var data = self.parse(result) ;
                var mycards = self.decode(data.player.cards);
                if(self.waittimer){
                    let timer = self.waittimer.getComponent("BeiMiTimer");
                    if(timer){
                        timer.stop(self.waittimer) ;
                    }
                }

                let center = self.game.pokerpool.get();
                let left = self.game.pokerpool.get(),right = self.game.pokerpool.get();
                center.parent = self.root() ;
                left.parent = self.root() ;
                right.parent = self.root() ;
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
                }, this , {game : self.game  , self: self, left :left , right : right , current : center});

                self.doLastCards(self.game , self , 3 , 0);

                /**
                 *  发牌动作，每次6张，本人保留总计17张，其他人发完即回收
                 */
                setTimeout(function() {
                    self.dealing(self.game , 6 , self , 0 , left , right , mycards) ;
                    setTimeout(function(){
                        self.dealing(self.game , 6 , self , 1, left , right , mycards) ;
                        setTimeout(function(){
                            self.dealing(self.game , 5 , self , 2, left , right , mycards , finished) ;
                            self.reordering(self);
                        },500) ;
                    },500) ;
                }, 0);
            });

        }
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
        this.socket.emit("giveup");
    },
    /**
     * 抢/叫地主触发事件
     */
    docatch:function(){
        this.socket.emit("docatch");
    },
    /**
     * 出牌
     */
    doPlayCards:function(){
        this.socket.emit("doplaycards" , this.game.selectedcards.join());
    },
    /**
     * 不出牌
     */
    noCards:function(){
        this.socket.emit("nocards");
    }
});
