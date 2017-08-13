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
    onClick:function(){
        let root = cc.find("Canvas");
        if (cc.tools.dialogpool.joinRoom.size() > 0) {
            cc.tools.dialog = cc.tools.dialogpool.joinRoom.get();
        
            if(cc.tools.dialog !== null){
                cc.tools.dialog.parent = root ;
                cc.tools.dialog.position = cc.p(0 , 0 ) ;
                
                cc.tools.dialog.on(cc.Node.EventType.TOUCH_START, function(e){
                    e.stopPropagation();
                });
            }
        }
    },
    onCloseClick:function(){
        if(cc.tools.dialog !== null){
            /**
             *  对象池返回， 释放资源 ，  同时 解除 事件绑定
             * 
             */
            cc.tools.dialog.off(cc.Node.EventType.TOUCH_START, function(e){
                e.stopPropagation();
            });
            cc.tools.dialogpool.joinRoom.put(cc.tools.dialog);
            cc.tools.dialog = null ;
        }
    },
    onJoinRoom:function(e){
        //Get Input Room Number , After : if Room Number exist , init room secene , else : tip message !
        this.onCloseClick(e);
        var rooNumber = "074653" ;
        /*****
         * 记录了进入的位置，从房卡进入房间 , 其他记录参数包括，进入的游戏类型等 ,  通过接口获取房间类型和玩法
         */
        cc.tools.control.joinRoom(rooNumber , "card" , "majiang" , "river");
    },
    onLeaveRoom:function(e){
        //Get Input Room Number , After : if Room Number exist , init room secene , else : tip message !
        cc.tools.control.leaveRoom();
    }


    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});
