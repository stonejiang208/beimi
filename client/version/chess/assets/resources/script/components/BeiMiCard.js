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
        card : cc.Integer,
        initcard :  {
            default: null,
            type: cc.Node
        },
        normal: {
            default: null,
            type: cc.Node
        },
        lefttop: {
            default: null,
            type: cc.Node
        },
        leftcolor: {
            default: null,
            type: cc.Node
        },
        rightbottom: {
            default: null,
            type: cc.Node
        },
        rightcolor: {
            default: null,
            type: cc.Node
        },
        kingbg:{
            default: null,
            type: cc.Node
        },
        king: {
            default: null,
            type: cc.Node
        },
        atlas: {
            default: null,
            type: cc.SpriteAtlas
        }
    },
    proxy:function(data){
        this.game = data ;
    },
    // use this for initialization
    onLoad: function () {
        this.initcard.active = true ;
        this.normal.active = false;
    },
    setCard:function(card){
        this.card = card ;
    },
    order:function(){

        let self = this ;
        var frame , cardframe ;
        if(self.card < 52){
            var cardvalue = self.card+1 ;
            if(cardvalue % 4 == 0){
                frame = this.atlas.getSpriteFrame('牌-方片');
            }else if(cardvalue % 4 == 1){
                frame = this.atlas.getSpriteFrame('牌-黑桃');
            }else if(cardvalue % 4 == 2){
                frame = this.atlas.getSpriteFrame('牌-红心');
            }else if(cardvalue % 4 == 3){
                frame = this.atlas.getSpriteFrame('牌-梅花');
            }
            var src = (self.card  - self.card % 4 ) / 4 + 1 + 2;
            if(src == 14){
                src = 1 ;
            }else if(src == 15){
                src = 2 ;
            }
            if(self.card % 2 == 0){
                cardframe = this.atlas.getSpriteFrame('牌-'+src);
            }else{
                cardframe = this.atlas.getSpriteFrame('牌-r'+src);
            }
            this.leftcolor.getComponent(cc.Sprite).spriteFrame = frame;
            this.lefttop.getComponent(cc.Sprite).spriteFrame = cardframe;
            this.rightcolor.getComponent(cc.Sprite).spriteFrame = frame;
            this.rightbottom.getComponent(cc.Sprite).spriteFrame = cardframe;


            this.initcard.active = false ;
            this.normal.active = true ;
            this.kingbg.active = false ;
        }else if(self.card == 52){
            frame = this.atlas.getSpriteFrame('牌-小王_大');
            this.king.getComponent(cc.Sprite).spriteFrame = frame;
            this.initcard.active = false ;
            this.normal.active = false;
            this.kingbg.active = true ;
        }else if(self.card == 53){
            frame = this.atlas.getSpriteFrame('牌-大王_大');
            this.king.getComponent(cc.Sprite).spriteFrame = frame;
            this.initcard.active = false ;
            this.normal.active = false;
            this.kingbg.active = true ;
        }

    }

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});
