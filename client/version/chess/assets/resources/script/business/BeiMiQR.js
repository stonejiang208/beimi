cc.Class({
	extends: cc.Component,

	properties: {

	},

	// use this for initialization
	onLoad: function() {
		if(cc.beimi!=null && cc.beimi.user!=null){
			var qrcode = new QRCode(6, QRErrorCorrectLevel.H);
			qrcode.addData('http://www.beixi.me');

			qrcode.make();

            let size = this.node.width;
            let num = qrcode.getModuleCount();
            var ctx = this.node.getComponent(cc.Graphics);
            ctx.clear();
            ctx.fillColor = cc.Color.BLACK;
            // compute tileW/tileH based on node width and height
            var tileW = size / num;
            var tileH = size / num;
            // draw in the Graphics
            for (var row = 0; row < num; row++) {
                for (var col = 0; col < num; col++) {
                    if (qrcode.isDark(row, col)) {
                        // cc.log(row, col)
                        // ctx.fillColor = cc.Color.BLACK;
                        var w = (Math.ceil((col + 1) * tileW) - Math.floor(col * tileW));
                        var h = (Math.ceil((row + 1) * tileW) - Math.floor(row * tileW));
                        ctx.rect(Math.round(col * tileW), size - tileH - Math.round(row * tileH), w, h);
                        ctx.fill();
                    } else {
                        // ctx.fillColor = cc.Color.WHITE;
                    }
                    // var w = (Math.ceil((col + 1) * tileW) - Math.floor(col * tileW));
                    // var h = (Math.ceil((row + 1) * tileW) - Math.floor(row * tileW));
                    // ctx.rect(Math.round(col * tileW), Math.round(row * tileH), w, h);
                    // ctx.fill();
                }
            }
        }
	},

	// called every frame, uncomment this function to activate update callback
	// update: function (dt) {

	// },
});