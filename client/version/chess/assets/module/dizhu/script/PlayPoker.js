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
        posy:cc.Integer,
        card:{
            default:null ,
            type : cc.Node
        }
    },

    // use this for initialization
    onLoad: function () {
        this.posy = this.card.y ;
        this.mouse_down  = false ;
        this.card.on(cc.Node.EventType.MOUSE_DOWN, function (event) {
            console.log('Mouse down');
        }, this);
        this.card.on(cc.Node.EventType.MOUSE_ENTER, function (event) {
            console.log('Mouse down');
        }, this);
    },
    takecard:function(){
        if(this.card.y == this.posy){
            this.card.y = this.card.y + 30 ;
        }else{
            this.card.y = this.card.y - 30 ;
        }
    }

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});
