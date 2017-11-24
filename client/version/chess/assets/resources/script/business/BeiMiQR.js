cc.Class({
	extends: cc.Component,

	properties: {

	},

	// use this for initialization
	onLoad: function() {
		if(cc.beimi!=null && cc.beimi.user!=null){
			var qrcode = new QRCode(-1, QRErrorCorrectLevel.H);
			qrcode.addData('http://www.beixi.me');
			qrcode.make();

			var ctx = this.node.getComponent(cc.Graphics);
			ctx.fillColor = cc.Color.BLACK;

			var width = this.node.width / qrcode.getModuleCount();
			var height = this.node.height / qrcode.getModuleCount();

			// draw in the Graphics
			for (var row = 0; row < qrcode.getModuleCount(); row++) {
				for (var col = 0; col < qrcode.getModuleCount(); col++) {
					if (qrcode.isDark(row, col)) {
						// ctx.fillColor = cc.Color.BLACK;
						var w = (Math.ceil((col + 1) * width) - Math.floor(col * width));
						var h = (Math.ceil((row + 1) * width) - Math.floor(row * width));
						ctx.rect(Math.round(col * width), Math.round(row * height), w, h);
						ctx.fill();
					} else {
						// ctx.fillColor = cc.Color.WHITE;
					}
					var w = (Math.ceil((col + 1) * width) - Math.floor(col * width));
					// var h = (Math.ceil((row + 1) * width) - Math.floor(row * width));
					// ctx.rect(Math.round(col * width), Math.round(row * height), w, h);
					// ctx.fill();
				}
			}
        }
	},

	// called every frame, uncomment this function to activate update callback
	// update: function (dt) {

	// },
});