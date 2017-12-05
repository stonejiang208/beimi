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
        atlas: {
            default: null,
            type: cc.SpriteAtlas
        },
        memo:{
            default:null ,
            type : cc.Label
        },
        roomtitle:{
            default:null ,
            type : cc.Node
        },
        optionitem:{
            default:null ,
            type : cc.Prefab
        },
        memonode:{
            default:null ,
            type : cc.Node
        },
        createroom:{
            default:null ,
            type : cc.Node
        },
        freeopt:{
            default:null ,
            type : cc.Node
        }
    },

    // use this for initialization
    onLoad: function () {

    },
    init:function(playway){
        this.data = playway ;
        if(this.memo != null && playway.memo!=null && playway.memo!=""){
            this.memonode.active = true ;
            this.memo.string = playway.memo ;
        }else if(this.memonode!=null){
            this.memonode.active = false ;
        }
        if(playway.free == true){
            this.freeopt.active = true;
            this.createroom.active = false ;
        }else{
            this.freeopt.active = false;
            this.createroom.active = true ;
        }
        if(playway.roomtitle!=null && playway.roomtitle!=""){
            let frame = this.atlas.getSpriteFrame(playway.roomtitle);
            if(frame!=null){
                this.roomtitle.getComponent(cc.Sprite).spriteFrame = frame ;
            }
        }
    }
    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});
