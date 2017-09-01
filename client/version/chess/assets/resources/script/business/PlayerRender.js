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
        }
    },

    // use this for initialization
    onLoad: function () {
        this.cardcount = 0 ;
    },
    initplayer:function(data , isRight){
        this.username.string = data.username ;
        this.userid = data.id ;

        if(isRight == true){
            this.pokertag.x = this.pokertag.x * -1;
            this.timer.x = this.timer.x * -1;
            this.headimg.x = this.headimg.x * -1
            this.result.x = this.result.x * -1
            this.jsq.x = this.jsq.x * -1
            this.dizhu.x = this.dizhu.x * -1
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
    },
    countcards:function(cards){
        this.cardcount = this.cardcount + cards ;
        this.pokercards.string = this.cardcount ;
    },
    catchtimer:function(){
        if(this.jsq){
            this.jsq.active = true ;
        }
        if(this.result){
            this.result.active = false ;
        }
        let self = this ;
        var gameTimer = require("GameTimer");
        this.beimitimer = new gameTimer();
        this.timesrc = this.beimitimer.runtimer(this , this.jsq , this.atlas , this.timer_first , this.timer_sec , 15);
    },
    catchresult:function(data){
        if(this.beimitimer){
            this.beimitimer.stoptimer(this , this.jsq , this.timesrc);
            var dograb = this.atlas.getSpriteFrame('提示_抢地主');
            var docatch = this.atlas.getSpriteFrame('提示_叫地主');
            if(data.grab){
                //抢地主
                if(this.result){
                    this.result.getComponent(cc.Sprite).spriteFrame = dograb;
                    this.result.active = true ;
                }
            }else{
                //叫地主
                if(this.result){
                    this.result.getComponent(cc.Sprite).spriteFrame = docatch;
                    this.result.active = true ;
                }
            }
        }
    },
    lasthands:function(self,data){      //所有玩家共用的
        if(this.result){
            this.result.active = false
        }
        if(this.userid == data.userid){//设置地主
            this.dizhu.active = true ;
            if(this.pokercards){
                this.countcards(3) ;
            }
        }else{
            this.dizhu.active = false ;
        }
    }
    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});
