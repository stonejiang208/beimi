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
        roomid: "",     //当前游戏中的房间号
        point:""        //进入房间的位置 ， 大厅进入/房卡模式进入/游戏类型
    },

    // use this for initialization
    onLoad: function () {

    },
    initGameSystem:function(){
        this._back = cc.find("Canvas/global/back");
        this._back.active = false ;
        
        this.disControl(true ,true );
        
        this._card = cc.find("Canvas/game/card");
        this._card.active = false ;
        this._card.opacity = 0 ;
        
        this._room = cc.find("Canvas/game/room");
        this._room.active = false ;
        this._room.opacity = 0 ;
    },
    enterCardControl:function(){
        
        this.disControl(true ,false);
        
        this._card = cc.find("Canvas/game/card");
        this._card.active = true ;
        this._card.opacity = 255 ;
        
        this._room = cc.find("Canvas/game/room");
        this._room.active = false ;
        this._room.opacity = 0 ;
        
        this.showBackBtn("room_back");
    },
    leaveCardControl:function(){
        this.disControl(true ,true );
        
        
        this._card = cc.find("Canvas/game/card");
        this._card.active = false ;
        this._card.opacity = 0 ;
        
        this._room = cc.find("Canvas/game/room");
        this._room.active = false ;
        this._room.opacity = 0 ;
        
        this.hiddenBackBtn();
    },
    enterRiverControl:function(){
        this.disControl(true ,false);
        
        this._card = cc.find("Canvas/game/card");
        this._card.active = false ;
        this._card.opacity = 0 ;
        
        this._room = cc.find("Canvas/game/room");
        this._room.active = false ;
        this._room.opacity = 0 ;
        
        this._girl = cc.find("Canvas/global/main/girl");
        this._animCtrl = this._girl.getComponent(cc.Animation);
        this._animCtrl.play("girl_to_left");
        
        this.showBackBtn("river_back");
        
    },
    leaveRiverControl:function(){
        this.disControl(true ,true );
        
        this._card = cc.find("Canvas/game/card");
        this._card.active = false ;
        this._card.opacity = 0 ;
        
        this._room = cc.find("Canvas/game/room");
        this._room.active = false ;
        this._room.opacity = 0 ;
        
        this.hiddenBackBtn();
        
        this._girl = cc.find("Canvas/global/main/girl");
        this._animCtrl = this._girl.getComponent(cc.Animation);
        this._animCtrl.play("girl_to_right");
    },
    enterEndControl:function(){
        this.disControl(true , false);
        
        this._card = cc.find("Canvas/game/card");
        this._card.active = false ;
        this._card.opacity = 0 ;
        
        this._room = cc.find("Canvas/game/room");
        this._room.active = false ;
        this._room.opacity = 0 ;
        
        this._girl = cc.find("Canvas/global/main/girl");
        this._animCtrl = this._girl.getComponent(cc.Animation);
        this._animCtrl.play("girl_to_left");
        
        this.showBackBtn("end_back");
        
    },
    leaveEndControl:function(){
        this.disControl(true ,true );
        
        
        this._card = cc.find("Canvas/game/card");
        this._card.active = false ;
        this._card.opacity = 0 ;
        
        this._room = cc.find("Canvas/game/room");
        this._room.active = false ;
        this._room.opacity = 0 ;
        
        this.hiddenBackBtn();
        
        this._girl = cc.find("Canvas/global/main/girl");
        this._animCtrl = this._girl.getComponent(cc.Animation);
        this._animCtrl.play("girl_to_right");
    },
    joinRoom:function(roomid ,  point , gametype ,  playway){  //房间ID ， 进入房间的大厅位置 ， 游戏类型 ， 玩法（血战/血流/其他）
        this.disControl(false ,false );
        /******
         *根据入口类型选择 进入的游戏场景 ， 如果 是通过房间号进入，则需要先查询房间号 
         */ 
        
        this._card = cc.find("Canvas/game/card");
        this._card.active = false ;
        this._card.opacity = 0 ;
        
        this._room = cc.find("Canvas/game/room");
        this._room.active = true ;
        this._room.opacity = 255 ;
        this.point = point ;
        this.roomid = roomid ;
        
        if(gametype && gametype === "majiang"){
            //River OR end , 血流成河 / 血战到底
            this.showPlayway(playway);
        }else if(gametype && gametype === "dizhu"){
            
        }else if(gametype && gametype === "dezhou"){
            
        }
    },
    leaveRoom:function(){
        this.disControl(false ,false );
        
        this._card = cc.find("Canvas/game/card");
        this._card.active = false ;
        this._card.opacity = 0 ;
        
        this._room = cc.find("Canvas/game/room");
        this._room.active = true ;
        this._room.opacity = 255 ;
        
        /**
         * 需要检查当前游戏是否已经结束或者未开始，如果是游戏中，做退出提示，强制退出后需要记录当前游戏的ROOMID
         */
         if(this.point === "card" ){ // 房卡 ，  退回房卡 
             this.enterCardControl();
         }
    },
    disControl:function(disGlobal , disSplash){
        this._global = cc.find("Canvas/global");
        if(disGlobal){
            this._global.active = true ;
        }else{
            this._global.active = false ;
        }
        this._global.opacity = 255 ;
        
        this._splash = cc.find("Canvas/splash");
        this._splash.opacity = 255 ;
        if(disSplash){
            this._splash.active = true ;
        }else{
            this._splash.active = false ;
        }

    },
    showBackBtn:function(name){
        this._back = cc.find("Canvas/global/back").children;
        for(i = 0 ;i < this._back.length ; i++){
            this._back[i].active = false ;
            if(this._back[i].name == name){
                this._back[i].active = true ;
            }
        }
        cc.find("Canvas/global/back").active = true ;
        
    },
    hiddenBackBtn:function(){
        this._back = cc.find("Canvas/global/back").children;
        for(i = 0 ;i < this._back.length ; i++){
            this._back[i].active = false ;
        }
        cc.find("Canvas/global/back").active =  false ; 
    },
    showPlayway:function(name){
        this._playway = cc.find("Canvas/game/room/majiang/playway").children;
        for(i = 0 ;i < this._playway.length ; i++){
            this._playway[i].active = false ;
            if(this._playway[i].name == name){
                this._playway[i].active = true ;
            }
        }
    },
    
    

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});
