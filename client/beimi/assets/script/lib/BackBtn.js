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
    }
    

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});
