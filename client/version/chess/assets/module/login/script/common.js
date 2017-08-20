cc.Class({
    extends: cc.Component,

    properties: {
        root: {
            default: null,
            type: cc.Node
        },
        prefab: {
            default: null,
            type: cc.Prefab
        }
    },

    // use this for initialization
    onLoad: function () {
        this.loginFormPool = new cc.NodePool();
        this.loginFormPool.put(cc.instantiate(this.prefab)); // 创建节点
    },
	login:function(){
		this.dialog = this.loginFormPool.get();
		this.dialog.parent = this.root ;
	},
	submit:function(){
		
	},
	guest:function(){
	
	},
	/**
	 *  传入手机号作为 注册用户信息
	 *
	 */
	register:function(){
		var xhr = cc.beimi.http.httpPost("/api/register",{token:cc.tools.http.authorization},function(ret){
            cc.beimi.http.authorization = ret ;
        });
	}

    // called every frame, uncomment this function to activate update callback
    // update: function (dt) {

    // },
});
