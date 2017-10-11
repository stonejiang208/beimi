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
        target:{
            default:null ,
            type : cc.Node
        }
    },

    // use this for initialization
    onLoad: function () {

    },
    onClick:function(event){
        //开始匹配
        let socket = this.socket();
        var param = {
            token:cc.beimi.authorization,
            playway:cc.beimi.playway,
            orgi:cc.beimi.user.orgi
        } ;
        let majiang = this.target.getComponent("MajiangDataBind");
        majiang.waittingForPlayers();
        socket.emit("joinroom" ,JSON.stringify(param)) ;
    }

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});
