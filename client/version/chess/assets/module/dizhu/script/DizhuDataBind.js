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
        goldcoins: {
            default: null,
            type: cc.Label
        },
        cards: {
            default: null,
            type: cc.Label
        },
        player: {
            default: null,
            type: cc.Prefab
        },
        poker: {
            default: null,
            type: cc.Prefab
        },
        myself: {
            default: null,
            type: cc.Prefab
        },
        atlas: {
            default: null,
            type: cc.SpriteAtlas
        },
        catchbtn: {
            default: null,
            type: cc.Node
        },
        timer: {
            default: null,
            type: cc.Node
        },
        timer_first:{
            default: null,
            type: cc.Node
        },
        timer_sec:{
            default: null,
            type: cc.Node
        },
        lastcards:{
            default: null,
            type: cc.Node
        },
        playbtn:{
            default: null,
            type: cc.Node
        }
    },

    // use this for initialization
    onLoad: function () {
        if(this.timer){
            this.timer.active = false ;
        }
        if(this.catchbtn){
            this.catchbtn.active = false ;
        }
        if(this.playbtn){
            this.playbtn.active = false ;
        }

        this.playerspool = new cc.NodePool();
        this.myselfpool = new cc.NodePool();
        this.pokerpool = new cc.NodePool();     //背面

        this.cardslist = new Array();

        for(i=0 ; i<2 ; i++){
            this.playerspool.put(cc.instantiate(this.player)); // 创建节点
        }
        for(i =0 ; i<35 ; i++){
            this.pokerpool.put(cc.instantiate(this.poker));     //牌-背面
        }
        this.myselfpool.put(cc.instantiate(this.myself));

        if(this.ready()){
            if(cc.beimi.user.goldcoins > 9999){
                var num = cc.beimi.user.goldcoins / 10000  ;
                this.goldcoins.string = num.toFixed(2) + '万';
            }else{
                this.goldcoins.string = cc.beimi.user.goldcoins;
            }
            this.cards.string = cc.beimi.user.cards + "张" ;
        }
        if(this.myselfpool.size() > 0 && cc.beimi){
            this.playermysql = this.myselfpool.get();
            this.playermysql.parent = this.root() ;
            this.playermysql.setPosition(-520,-180);
            var render = this.playermysql.getComponent("PlayerRender") ;
            render.initplayer(cc.beimi.user);
        }
    },
    catchtimer:function(times){

        if(this.playbtn){
            this.playbtn.active = false ;
        }
        if(this.timer){
            this.timer.active = true ;
        }
        if(this.catchbtn){
            this.catchbtn.active = true ;
        }
        let self = this ;
        var gameTimer = require("GameTimer");
        this.beimitimer = new gameTimer();
        this.timesrc = this.beimitimer.runtimer(this , this.timer , this.atlas , this.timer_first , this.timer_sec , times);
    },
    catchresult:function(){
        if(this.timer){
            this.timer.active = false ;
        }
        if(this.catchbtn){
            this.catchbtn.active = false ;
        }
        if(this.playbtn){
            this.playbtn.active = false ;
        }
        if(this.timesrc){
            this.beimitimer.stoptimer(this , this.timer , this.timesrc);
        }
    },
    lasthands:function(self, game,data){   //设置地主
        var render = this.playermysql.getComponent("PlayerRender") ;
        render.lasthands(self ,game , data) ;
    },
    lasttakecards:function(game , self , cardsnum ,lastcards){
        if(this.result){
            this.result.active = false ;
        }
        if(this.playbtn){
            this.playbtn.active = false ;
        }
        if(this.catchbtn){
            this.catchbtn.active = false;
        }
        if(this.jsq){
            this.jsq.active = false ;
        }
        if(this.lastcards){
            this.lastcards.active = true ;
        }
        if(this.timesrc){
            this.beimitimer.stoptimer(this , this.timer , this.timesrc);
        }
        for(var i=0 ; i<this.cardslist.length ; i++){
            game.pokerpool.put(this.cardslist[i]) ;//回收回去
        }
        this.cardslist.splice(0,this.cardslist.length) ;//删除数组里的所有内容
        for(var i=0 ; i<lastcards.length ; i++){
            this.playcards(self , i , lastcards[i]) ;
        }
    },

    playcards:function(self , index , card){
        var cacheCard ;
        for(var inx = 0 ; inx<self.pokercards.length ; inx++){
            let pc = self.pokercards[inx] ;
            if(pc.card == card){
                cacheCard = pc ; break ;
            }
        }
        if(cacheCard != null){
            cacheCard.x = index * 30 - 30 ;
            cacheCard.y = 0;

            cacheCard.zIndex = i ;

            cacheCard.setScale(0.5,0.5);

            cacheCard.parent = this.lastcards ;


            this.cardslist[this.cardslist.length] = cacheCard ;
        }
    },
    playtimer:function(game , times){
        if(this.timer){
            this.timer.active = true ;
        }
        if(this.playbtn){
            this.playbtn.active = true ;
        }
        if(this.catchbtn){
            this.catchbtn.active = false;
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
        this.timesrc = this.beimitimer.runtimer(this , this.timer , this.atlas , this.timer_first , this.timer_sec , times);
    }

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});
