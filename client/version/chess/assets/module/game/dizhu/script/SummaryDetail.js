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
        myscore:{   //底牌
            default: null,
            type: cc.Label
        },
        myflag:{
            default: null,
            type: cc.Node
        },
        player_1:{
            default: null,
            type: cc.Node
        },
        player_1_flag:{
            default: null,
            type: cc.Node
        },
        player_1_name:{
            default: null,
            type: cc.Label
        },
        player_1_score:{
            default: null,
            type: cc.Label
        },
        player_2:{
            default: null,
            type: cc.Node
        },
        player_2_flag:{
            default: null,
            type: cc.Node
        },
        player_2_name:{
            default: null,
            type: cc.Label
        },
        player_2_score:{
            default: null,
            type: cc.Label
        },
    },

    // use this for initialization
    onLoad: function () {

    },
    create:function(data){

    }

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});
