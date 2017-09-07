cc.Class({
    extends: cc.Component,

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
        username: {
            default: null,
            type: cc.Label
        },
        goldcoins: {
            default: null,
            type: cc.Label
        },
        dizhu: {
            default: null,
            type: cc.Node
        },
        pokertag: {
            default: null,
            type: cc.Node
        },
        pokercards: {
            default: null,
            type: cc.Label
        },
        timer:{
            default: null,
            type: cc.Node
        },
        jsq:{
            default: null,
            type: cc.Node
        },
        headimg:{
            default: null,
            type: cc.Node
        },
        atlas: {
            default: null,
            type: cc.SpriteAtlas
        },
        timer_first:{
            default: null,
            type: cc.Node
        },
        timer_sec:{
            default: null,
            type: cc.Node
        },
        result:{
            default: null,
            type: cc.Node
        },
        lastcards:{
            default: null,
            type: cc.Node
        },
        cannot:{
            default: null,
            type: cc.Node
        },
        donot:{
            default: null,
            type: cc.Node
        }
    },

    // use this for initialization
    onLoad: function () {
        this.cardcount = 0 ;
        this.cardslist = new Array();
        this.isRight = false ;
    },
    initplayer:function(data , isRight){
        this.username.string = data.username ;
        this.userid = data.id ;

        if(isRight == true){
            this.pokertag.x = this.pokertag.x * -1;
            this.timer.x = this.timer.x * -1;
            this.headimg.x = this.headimg.x * -1
            this.result.x = this.result.x * -1

            this.cannot.x = this.cannot.x * -1
            this.donot.x = this.donot.x * -1

            this.jsq.x = this.jsq.x * -1
            this.dizhu.x = this.dizhu.x * -1
            // this.lastcards.x = this.lastcards.x * -1
            this.lastcards.getComponent(cc.Layout).horizontalDirection = 1 ;
            this.isRight = isRight ;
        }
        if(this.goldcoins){
            if(data.goldcoins > 10000){
                var num = this.goldcoins / 10000  ;
                this.goldcoins.string = num.toFixed(2) + '万';
            }else{
                this.goldcoins.string = data.goldcoins;
            }
        }
        if(this.dizhu){
            this.dizhu.active = false ;
        }
        if(this.jsq){
            this.jsq.active = false ;
        }
        if(this.result){
            this.result.active = false ;
        }
        if(this.cannot){
            this.cannot.active = false ;
        }
        if(this.donot){
            this.donot.active = false ;
        }
        if(this.takecards){
            this.takecards.active = false ;
        }
    },
    countcards:function(cards){
        this.cardcount = this.cardcount + cards ;
        this.pokercards.string = this.cardcount ;
    },
    resetcards:function(cards){
        this.cardcount = cards ;
        this.pokercards.string = this.cardcount ;
    },
    catchtimer:function(times){
        if(this.jsq){
            this.jsq.active = true ;
        }
        if(this.result){
            this.result.active = false ;
        }
        if(this.cannot){
            this.cannot.active = false ;
        }
        if(this.donot){
            this.donot.active = false ;
        }
        let self = this ;
        var gameTimer = require("GameTimer");
        this.beimitimer = new gameTimer();
        this.timesrc = this.beimitimer.runtimer(this , this.jsq , this.atlas , this.timer_first , this.timer_sec , times);
    },
    catchresult:function(data){
        if(this.beimitimer){
            this.beimitimer.stoptimer(this , this.jsq , this.timesrc);
            var dograb = this.atlas.getSpriteFrame('提示_抢地主');
            var docatch = this.atlas.getSpriteFrame('提示_不抢');
            if(data.grab){
                //抢地主
                if(this.result){
                    this.result.getComponent(cc.Sprite).spriteFrame = dograb;
                    this.result.active = true ;
                }
                if(this.cannot){
                    this.cannot.active = false ;
                }
                if(this.donot){
                    this.donot.active = false ;
                }
            }else{
                //叫地主
                if(this.result){
                    this.result.getComponent(cc.Sprite).spriteFrame = docatch;
                    this.result.active = true ;
                }
                if(this.cannot){
                    this.cannot.active = false ;
                }
                if(this.donot){
                    this.donot.active = false ;
                }
            }
        }
    },
    hideresult:function(){
        if(this.result){
            this.result.active = false ;
        }
        if(this.cannot){
            this.cannot.active = false ;
        }
        if(this.donot){
            this.donot.active = false ;
        }
    },
    lasthands:function(self, game ,data){      //所有玩家共用的
        this.hideresult();
        if(this.beimitimer && this.timesrc) {
            this.beimitimer.stoptimer(this, this.jsq, this.timesrc);
        }
        if(this.userid == data.userid){//设置地主
            if(this.pokercards){
                this.countcards(3) ;
            }
            /**
             * 开始计时
             */
            this.playtimer(game , 25) ;
        }
        this.setDizhuFlag(data);
    },
    setDizhuFlag:function(data){
        if(this.userid == data.userid){//设置地主
            this.dizhu.active = true ;
        }else{
            this.dizhu.active = false ;
        }
    },
    lasttakecards:function(game , self , cardsnum ,lastcards ,data) {
        if (this.beimitimer && this.timesrc) {
            this.beimitimer.stoptimer(this, this.jsq, this.timesrc);
        }
        if (this.result) {
            this.result.active = false;
        }
        if (this.cannot) {
            this.cannot.active = false;
        }
        if (this.donot) {
            this.donot.active = false;
        }
        if (this.jsq) {
            this.jsq.active = false;
        }
        if (this.lastcards) {
            this.lastcards.active = true;
        }
        for (var i = 0; i < this.cardslist.length; i++) {
            game.pokerpool.put(this.cardslist[i]);//回收回去
        }
        this.cardslist.splice(0, this.cardslist.length);//删除数组里的所有内容
        if (data.donot == false) {
            this.resetcards(cardsnum);

            for (var i = 0; i < lastcards.length; i++) {
                this.playcards(game, i, lastcards, lastcards[i]);
            }
        }else{
            if(data.sameside == "1"){
                self.getPlayer(data.userid).tipdonot();
            }else{
                self.getPlayer(data.userid).tipcannot();
            }
        }
    },
    tipcannot:function(){
        if(this.result){
            this.result.active = false ;
        }
        if(this.cannot){
            this.cannot.active = true ;
        }
        if(this.donot){
            this.donot.active = false ;
        }
    },
    tipdonot:function(){
        if(this.result){
            this.result.active = false ;
        }
        if(this.cannot){
            this.cannot.active = false ;
        }
        if(this.donot){
            this.donot.active = true ;
        }
    },
    playcards:function(game , index, lastcards , card){
        let currpoker = game.pokerpool.get() ;
        currpoker.card = card ;

        currpoker.setScale(0.5,0.5);

        currpoker.parent = this.lastcards ;

        currpoker.x = index * 30 - 30 ;
        currpoker.y = 0;
        currpoker.zIndex = lastcards.length - index;

        let beiMiCard = currpoker.getComponent("BeiMiCard");
        beiMiCard.setCard(card) ;
        beiMiCard.order();

        console.log("牌的位置:"+currpoker.y) ;

        this.cardslist[this.cardslist.length] = currpoker ;
    },
    playtimer:function(game , times){
        if(this.result){
            this.result.active = false ;
        }
        if(this.cannot){
            this.cannot.active = false ;
        }
        if(this.donot){
            this.donot.active = false ;
        }
        if(this.lastcards){
            this.lastcards.active = false ;
        }
        for(var i=0 ; i<this.cardslist.length ; i++){
            game.pokerpool.put(this.cardslist[i]) ;//回收回去
        }
        let self = this ;
        var gameTimer = require("GameTimer");
        this.beimitimer = new gameTimer();
        this.timesrc = this.beimitimer.runtimer(this , this.jsq , this.atlas , this.timer_first , this.timer_sec , times);
    }
    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});
