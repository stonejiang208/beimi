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
        }
    },

    // use this for initialization
    onLoad: function () {
        this.cardcount = 0 ;
    },
    initplayer:function(data , inx){
        this.username.string = data.username ;
        this.userid = data.id ;
        if(inx == 1){
            this.pokertag.x = this.pokertag.x * -1;
            this.timer.x = this.timer.x * -1;
            this.headimg.x = this.headimg.x * -1
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
        if(this.timer){
            this.timer.active = false ;
        }
    },
    countcards:function(cards){
        this.cardcount = this.cardcount + cards ;
        this.pokercards.string = this.cardcount ;
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
