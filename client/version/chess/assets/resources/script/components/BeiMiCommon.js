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
        cc.beimi.room_callback = null ;  //加入房间回调函数
    },
    ready:function(){
        var check = false ;
        if(cc.beimi){
            check = true ;
        }else{
            this.scene("login" , this) ;
        }
        return check ;
    },
    connect:function(){
        /**
         * 登录成功后，创建 Socket链接，
         */
        if(cc.beimi.socket != null){
            cc.beimi.socket.disconnect();
            cc.beimi.socket = null ;
        }
        cc.beimi.socket = window.io.connect(cc.beimi.http.wsURL + '/bm/game');
        var param = {
            token:cc.beimi.authorization,
            orgi:cc.beimi.user.orgi
        } ;
        let self = this ;
        cc.beimi.socket.emit("gamestatus" , JSON.stringify(param));
        cc.beimi.socket.on("gamestatus" , function(result){
            if(result!=null) {
                var data = self.parse(result) ;
                cc.beimi.gamestatus = data.gamestatus;
            }
        });
        /**
         * 加入房卡模式的游戏类型 ， 需要校验是否是服务端发送的消息
         */
        cc.beimi.socket.on("searchroom" , function(result){
            //result 是 GamePlayway数据，如果找到了 房间数据，则进入房间，如果未找到房间数据，则提示房间不存在
            if(result!=null && cc.beimi.room_callback!=null) {
                cc.beimi.room_callback(result , self);
            }
        });
        return cc.beimi.socket ;
    },
    disconnect:function(){
        if(cc.beimi.socket != null){
            cc.beimi.socket.disconnect();
            cc.beimi.socket = null ;
        }
    },
    registercallback:function(callback){
        cc.beimi.room_callback = callback ;
    },
    cleancallback:function(){
        cc.beimi.room_callback = null ;
    },
    getCommon:function(common){
        var object = cc.find("Canvas/script/"+common) ;
        return object.getComponent(common);
    },
    loadding:function(){
        if(cc.beimi.loadding.size() > 0){
            this.loaddingDialog = cc.beimi.loadding.get();
            this.loaddingDialog.parent = cc.find("Canvas");

            this._animCtrl = this.loaddingDialog.getComponent(cc.Animation);
            var animState = this._animCtrl.play("loadding");
            animState.wrapMode = cc.WrapMode.Loop;
        }
    },
    alert:function(message){
        if(cc.beimi.dialog.size() > 0){
            this.alertdialog = cc.beimi.dialog.get();
            this.alertdialog.parent = cc.find("Canvas");
            let node = this.alertdialog.getChildByName("message") ;
            if(node!=null && node.getComponent(cc.Label)){
                node.getComponent(cc.Label).string = message ;
            }
        }
        this.closeloadding();
    },
    closeloadding:function(){
        if(cc.find("Canvas/loadding")){
            cc.beimi.loadding.put(cc.find("Canvas/loadding"));
        }
    },
    closeOpenWin:function(){
        if(cc.beimi.openwin != null){
            cc.beimi.openwin.destroy();
            cc.beimi.openwin = null ;
        }
    },
    resize:function(){
        let win = cc.director.getWinSize() ;
        cc.view.setDesignResolutionSize(win.width, win.height, cc.ResolutionPolicy.EXACT_FIT);
    },
    closealert:function(){
        if(cc.find("Canvas/alert")){
            cc.beimi.dialog.put(cc.find("Canvas/alert"));
        }
    },
    scene:function(name , self){
        cc.director.preloadScene(name, function () {
            if(cc.beimi){
                self.closeloadding(self.loaddingDialog);
            }
            cc.director.loadScene(name);
        });
    },
    root:function(){
        return cc.find("Canvas");
    },
    decode:function(data){
        var cards = new Array();

        if(!cc.sys.isNative) {
            var dataView = new DataView(data);
            for(var i= 0 ; i<data.byteLength ; i++){
                cards[i] = dataView.getInt8(i);
            }
        }else{
            var Base64 = require("Base64");
            var strArray = Base64.decode(data) ;

            if(strArray && strArray.length > 0){
                for(var i= 0 ; i<strArray.length ; i++){
                    cards[i] = strArray[i];
                }
            }
        }

        return cards ;
    },
    parse(result){
        var data ;
        if(!cc.sys.isNative){
            data = result;
        }else{
            data = JSON.parse(result) ;
        }
        return data ;
    },
    reset:function(data , result){
        //放在全局变量
        cc.beimi.authorization = data.token.id ;
        cc.beimi.user = data.data ;
        cc.beimi.games = data.games ;
        cc.beimi.gametype = data.gametype ;

        cc.beimi.playway = null ;
        this.io.put("userinfo" ,result );
    },
    logout:function(){
        this.closeOpenWin();
        cc.beimi.authorization = null ;
        cc.beimi.user = null ;
        cc.beimi.games = null ;

        cc.beimi.playway = null ;

        this.disconnect();
    },
    socket:function(){
        let socket = cc.beimi.socket ;
        if(socket == null){
            socket = this.connect();
        }
        return socket ;
    },
    map:function(command, callback){
        if(cc.routes[command] == null){
            cc.routes[command] = callback || function(){};
        }
    },
    route:function(command){
        return cc.routes[command] || function(){};
    },
    /**
     * 解决Layout的渲染顺序和显示顺序不一致的问题
     * @param target
     * @param func
     */
    layout:function(target , func){
        if(target != null){
            let temp = new Array() ;
            let children = target.children ;
            for(var inx = 0 ; inx < children.length ; inx++){
                temp.push(children[inx]) ;
            }
            for(var inx = 0 ; inx < temp.length ; inx++){
                target.removeChild(temp[inx]) ;
            }

            temp.sort(func) ;
            for(var inx =0 ; inx<temp.length ; inx++){
                temp[inx].parent = target ;
            }
            temp.splice(0 , temp.length) ;
        }
    }

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});
