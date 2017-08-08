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
    },

    // use this for initialization
    onLoad: function () {
        //**
    },
    onClick:function(){
        this._splash = cc.find("Canvas/splash");
        this._splash.active =  false;
        
        this._girl = cc.find("Canvas/global/main/girl");
        this._animCtrl = this._girl.getComponent(cc.Animation);
        this._animCtrl.play("girl_to_left");
        
        cc.tools.back.showBackBtn("river_back");
    }

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});
