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
        first: {
            default: null,
            type: cc.Node
        },
        second: {
            default: null,
            type: cc.Node
        },
        third: {
            default: null,
            type: cc.Node
        },
        title:{
            default: null,
            type: cc.Node
        },
        global: {
            default: null,
            type: cc.Node
        },
        playway: {
            default: null,
            type: cc.Prefab
        },
        content: {
            default: null,
            type: cc.Node
        },
    },

    // use this for initialization
    onLoad: function () {
        this.disMenu("first") ;
        this.playwaypool = new cc.NodePool();
        for(var i=0 ; i<20 ; i++){ //最大玩法数量不能超过20种
            this.playwaypool.put(cc.instantiate(this.playway));
        }
        this.playwayarray = new Array();
    },
    onClick:function(event, data){
        this.disMenu("second") ;
        var girlAni = this.global.getComponent("DefaultHallDataBind");
        girlAni.playToLeft();
        this._secondAnimCtrl = this.second.getComponent(cc.Animation);
        this._secondAnimCtrl.play("playway_display");

        if(this.title){
            for(var inx = 0 ; inx<this.title.children.length ; inx++){
                if(this.title.children[inx].name == data){
                    this.title.children[inx].active = true ;
                }else{
                    this.title.children[inx].active = false ;
                }

            }
        }
        /**
         * 加载预制的 玩法
         */
        var gametype = cc.beimi.game.type(data);
        if(gametype!=null){
            for(var inx =0 ; inx < gametype.playways.length ; inx++){
                /**
                 * 此处需要做判断，检查 对象池有足够的对象可以使用
                 */
                var playway = this.playwaypool.get();
                var script = playway.getComponent("Playway") ;
                script.init(gametype.playways[inx]);
                playway.parent = this.content ;
                this.playwayarray.push(playway) ;
            }
        }
    },
    onRoomClick:function(){
        this.disMenu("third") ;
        this._menuDisplay = this.third.getComponent(cc.Animation);
        this._menuDisplay.play("play_room_display");
    },
    onSecondBack:function(event ,data){
        var girlAni = this.global.getComponent("DefaultHallDataBind");
        girlAni.playToRight();
        this.collect();
        this.disMenu("first") ;
    },
    onThirddBack:function(event ,data){
        this.disMenu("first") ;
    },
    collect:function(){
        for(var inx =0 ; inx < this.playwayarray.length ; inx++){
            this.playwaypool.put(this.playwayarray[inx]);
        }
        this.playwayarray.splice(0 ,this.playwayarray.length );
    },
    disMenu:function(order){
        if(order == 'first'){
            this.first.active = true ;
            this.second.active = false ;
            if(this.third != null){
                this.third.active = false ;
            }
        }else if(order == 'second'){
            this.first.active = false;
            this.second.active = true;
            if(this.third != null){
                this.third.active = false ;
            }
        }else if(order == 'third'){
            this.first.active = false;
            this.second.active = false;
            if(this.third != null){
                this.third.active = true ;
            }
        }
    }
    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});
