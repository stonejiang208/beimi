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
        game: {
            default: null,
            type: cc.Node
        },
        poker: {
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
    },
    begin:function(){
        this.game.active = false ;
        this.initgame(false);
        this.waittimer = cc.instantiate(this.waitting);
        this.waittimer.parent = this.root();

        let timer = this.waittimer.getComponent("BeiMiTimer");
        if(timer){
            timer.init("正在匹配玩家" , 5 , this.waittimer);
        }
    },
    opendeal:function(){
        this.game.active = false ;
        this.initgame(true);
    },
    initgame:function(opendeal){
        let self = this ;
        /**
         * opendeal:明牌
         * playway:传入的玩法参数
         * 连接服务器，进入ROOM，开始等待其他玩家或AI加入 , 发送 创建ROOM的 HTTP请求，创建 ROOM ，ROOM创建成功后开始建立 SocketIO链接
         */
        if(cc.beimi && cc.beimi.authorization && cc.beimi.user){
            var socket = window.io.connect('http://192.168.1.155:9081/bm/game?token='+cc.beimi.authorization+"&playway=402888815e21d735015e21d995680000&orgi="+cc.beimi.user.orgi);
            socket.on("connect" , function(){
               //显示 匹配中，并计时间，超过设定的倒计时时间即AI加入，根据当前的 玩家数量 匹配机器人
                //console.log("testabc");
            });
            socket.on("joinroom" , function(result){
                var data = JSON.parse(result);
                //显示 匹配中，并计时间，超过设定的倒计时时间即AI加入，根据当前的 玩家数量 匹配机器人
                if(data.id && data.id == cc.beimi.user.id){
                    //本人，开始计时
                    //console.log("本人") ;
                    //self.player[0] = data ;
                }else{
                    //其他玩家加入，初始化
                    var inroom = false ;
                    for(var i = 0 ; i < self.player.length ; i++){
                        if(self.player[i].id == data.id){
                            inroom = true ;
                        }
                    }
                    if(inroom == false){
                        self.newplayer(self.player.length , self , data) ;
                    }
                }
            });
            socket.on("players" , function(result){
                var data = JSON.parse(result);
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
            this.game = self.getCommon("DizhuDataBind");
            socket.on("play" , function(result){
                var data = JSON.parse(result) ;
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

                    }
                }, this , {game : self.game  , self: self, left :left , right : right , current : center});

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
    dealing:function(game , num , self , times , left , right , cards , finished){
        /**
         * 处理当前玩家的 牌， 发牌 ，  17张牌， 分三次动作处理完成
         */
        for(var i=0 ; i<num ; i++){
            self.current(game , self ,times * 300 + i * 50-300, cards[times * 6 + i] , finished) ;
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
    current:function(game , self , posx , card , func){
        let currpoker = game.pokerpool.get() ;

        currpoker.getComponent("BeiMiCard").init(card) ;

        currpoker.card = card ;
        currpoker.parent = self.root() ;
        currpoker.setPosition(0,200);
        self.pokercards[self.pokercards.length] = currpoker ;
        let seq = cc.sequence(cc.moveTo(0.2, posx, -180) , func);
        currpoker.runAction(seq);
    },
    reordering:function(self){
        for(var i=0 ; i<self.pokercards.length ; i++){
            self.pokercards[i].parent = self.poker ;
        }
    },
    newplayer:function(inx , self , data){
        var pos = cc.v2(520, 100) ;
        if(inx == 0){
            pos = cc.v2(-520,100) ;
        }
        let game = self.getCommon("DizhuDataBind");
        if(game && game.playerspool.size() > 0){
            self.player[inx] = game.playerspool.get() ;
            self.player[inx].parent = self.root() ;
            self.player[inx].setPosition(pos);
            var render = self.player[inx].getComponent("PlayerRender") ;
            render.initplayer(data , inx);
        }
    }

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});
