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
        grouptitle:{
            default:null ,
            type : cc.Label
        },
        groupbox:{
            default:null ,
            type : cc.Node
        },
        groupbox_four:{
            default:null ,
            type : cc.Node
        },
        itemname:{
            default:null ,
            type : cc.Label
        },
        checkbox:{
            default:null ,
            type : cc.Node
        },
        checkboxnode:{
            default:null ,
            type : cc.Node
        }
    },

    // use this for initialization
    onLoad: function () {
        let self = this ;
        this.checked = false ;
        this.node.on('checkbox', function (event) {
            if(self.checkbox!=null){
                if(self.checked == false){
                    self.doChecked();
                }else{
                    self.doUnChecked();
                }
            }
            event.stopPropagation() ;
        });
    },
    init:function(group , itempre , items){
        this.grouptitle.string = group.name ;
        if(this.groupbox!=null && itempre!=null){
            for(var inx=0 ; inx<items.length ; inx++){
                if(items[inx].groupid == group.id){
                    let newitem = cc.instantiate(itempre) ;
                    if(group.style != null && group.style == "three"){
                        newitem.parent = this.groupbox ;
                        this.groupbox_four.active = false ;
                        this.groupbox.active = true ;
                    }else{
                        newitem.parent = this.groupbox_four ;
                        this.groupbox_four.active = true;
                        this.groupbox.active = false;
                    }
                    let script = newitem.getComponent("PlaywayGroup") ;
                    script.inititem(items[inx] , group);
                }
            }
        }
    },
    inititem:function(item , group){
        this.itemname.string = item.name ;
        if(item.defaultvalue == true){
            this.checkbox.active = true ;
        }else{
            this.checkbox.active = false ;
        }
        if(group!=null && group.style!=null && group.style == "three"){
            this.checkboxnode.x = -83 ;
        }
    },
    doChecked:function(){
        this.checked = true ;
        this.checkbox.active = true ;
    },
    doUnChecked:function(){
        this.checked = false ;
        this.checkbox.active = false;
    }

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});
