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
        }
    },

    // use this for initialization
    onLoad: function () {
        if(this.timer){
            this.timer.active = false ;
        }

        this.playerspool = new cc.NodePool();
        this.myselfpool = new cc.NodePool();
        this.pokerpool = new cc.NodePool();     //背面
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
            let myself = this.myselfpool.get();
            myself.parent = this.root() ;
            myself.setPosition(-500,-180);
            var render = myself.getComponent("PlayerRender") ;
            render.initplayer(cc.beimi.user);
        }
    },
    catchtimer:function(){
        if(this.timer){
            this.timer.active = true ;
        }
        let self = this ;
        if(this.atlas){
            var timer_first_num = this.atlas.getSpriteFrame('jsq1');
            var timer_sec_num = this.atlas.getSpriteFrame('jsq5');
            this.timer_first.getComponent(cc.Sprite).spriteFrame = timer_first_num;
            this.timer_sec.getComponent(cc.Sprite).spriteFrame = timer_sec_num;
            /**
             * 15秒计时，最长不超过15秒
             */
            self.remaining = 15 ;
            this.timersc = this.schedule(function() {
                self.remaining = self.remaining - 1 ;
                if(self.remaining < 0){
                    self.unschedule(this);
                    self.timer.active = false ;
                }else{
                    if(self.remaining<10){
                        timer_first_num = self.atlas.getSpriteFrame('jsq0')
                    }else{
                        timer_first_num = self.atlas.getSpriteFrame('jsq1')
                    }
                    timer_sec_num = self.atlas.getSpriteFrame('jsq'+self.remaining % 10) ;
                    self.timer_first.getComponent(cc.Sprite).spriteFrame = timer_first_num;
                    self.timer_sec.getComponent(cc.Sprite).spriteFrame = timer_sec_num;
                }
            }, 1 , 15 , 0);
        }
    }

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});
