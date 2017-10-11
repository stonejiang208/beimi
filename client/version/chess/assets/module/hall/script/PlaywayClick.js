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
        // ..
        playway:{
            default: null,
            type: cc.Node
        },
    },

    // use this for initialization
    onLoad: function () {

    },
    onClick:function(){
        let self = this ;
        this.loadding();
        var selectPlayway = this.getCommon("SelectPlayway");

        let thisplayway = this.playway.getComponent("Playway");

        cc.beimi.playway = thisplayway.data.id ;

        setTimeout(function(){
            /**
             * 优化交互，预加载场景完毕后再回收资源
             */
            selectPlayway.collect();
            self.scene(thisplayway.data.code , self) ;
        },200);
    },
    createRoom:function(event,data){
        let self = this ;
        this.loadding();
        setTimeout(function(){
            self.scene(data, self) ;
        },200);
    }

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});
