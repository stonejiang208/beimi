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

    },
    /**
     * 结算页面上的 背景的 点击事件，主要是用于事件拦截，禁用冒泡
     * @param event
     */
    onBGClick:function(event){
        event.stopPropagation();
    },
    /**
     * 结算页面上的关闭按钮 的 点击事件 , 关闭按钮 和 继续按钮 功能是一样的，都是继续游戏
     */
    onCloseClick:function(){

    }

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});
