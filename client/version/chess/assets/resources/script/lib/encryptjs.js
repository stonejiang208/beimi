/*!
 * Copyright (c) 2015 Sri Harsha <sri.harsha@zenq.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

(function (name, definition) {
    if (typeof exports !== 'undefined' && typeof module !== 'undefined') {
        module.exports = definition();
    } else if (typeof define === 'function' && typeof define.amd === 'object') {
        define(definition);
    } else if (typeof define === 'function' && typeof define.petal === 'object') {
        define(name, [], definition);
    } else {
        this[name] = definition();
    }
})('encryptjs', function (encryptjs) {

    'use strict';
    var readline = require('readline');
    var fs=require('fs');
    var rl;
    //Electron doesnt support stdin, so dont setup CLI if its not available.
    try {
        rl = readline.createInterface({
            input: process.stdin,
            output: process.stdout
			  });
    } catch (e) {
        rl = null;
        console.log('Command line is not supported on this platform', e);
    }
    encryptjs = { version: '1.0.0' };

    //Right before exporting the validator object, pass each of the builtins
    //through extend() so that their first argument is coerced to a string
    encryptjs.init = function () {
        console.log("--------------------Applying Encryption Algorithm------------------ ");
    };
    'use strict';
    if (typeof module!='undefined' && module.exports) var Algo = require('./algo'); // CommonJS (Node.js)

    encryptjs.encrypt = function(plaintext, password, nBits) {
        var blockSize = 16;  // block size fixed at 16 bytes / 128 bits (Nb=4)
        if (!(nBits==128 || nBits==192 || nBits==256)) return ''; // standard allows 128/192/256 bit keys
        plaintext = String(plaintext).utf8Encode();
        password = String(password).utf8Encode();

        // use AES itself to encrypt password to get cipher key (using plain password as source for key
        // expansion) - gives us well encrypted key (though hashed key might be preferred for prod'n use)
        var nBytes = nBits/8;  // no bytes in key (16/24/32)
        var pwBytes = new Array(nBytes);
        for (var i=0; i<nBytes; i++) {  // use 1st 16/24/32 chars of password for key
            pwBytes[i] = isNaN(password.charCodeAt(i)) ? 0 : password.charCodeAt(i);
        }
        var key = Algo.cipher(pwBytes, Algo.keyExpansion(pwBytes)); // gives us 16-byte key
        key = key.concat(key.slice(0, nBytes-16));  // expand key to 16/24/32 bytes long

        // initialise 1st 8 bytes of counter block with nonce (NIST SP800-38A �B.2): [0-1] = millisec,
        // [2-3] = random, [4-7] = seconds, together giving full sub-millisec uniqueness up to Feb 2106
        var counterBlock = new Array(blockSize);

        var nonce = (new Date()).getTime();  // timestamp: milliseconds since 1-Jan-1970
        var nonceMs = nonce%1000;
        var nonceSec = Math.floor(nonce/1000);
        var nonceRnd = Math.floor(Math.random()*0xffff);
        // for debugging: nonce = nonceMs = nonceSec = nonceRnd = 0;

        for (var i=0; i<2; i++) counterBlock[i]   = (nonceMs  >>> i*8) & 0xff;
        for (var i=0; i<2; i++) counterBlock[i+2] = (nonceRnd >>> i*8) & 0xff;
        for (var i=0; i<4; i++) counterBlock[i+4] = (nonceSec >>> i*8) & 0xff;

        // and convert it to a string to go on the front of the ciphertext
        var ctrTxt = '';
        for (var i=0; i<8; i++) ctrTxt += String.fromCharCode(counterBlock[i]);

        // generate key schedule - an expansion of the key into distinct Key Rounds for each round
        var keySchedule = Algo.keyExpansion(key);

        var blockCount = Math.ceil(plaintext.length/blockSize);
        var ciphertxt = new Array(blockCount);  // ciphertext as array of strings

        for (var b=0; b<blockCount; b++) {
            // set counter (block #) in last 8 bytes of counter block (leaving nonce in 1st 8 bytes)
            // done in two stages for 32-bit ops: using two words allows us to go past 2^32 blocks (68GB)
            for (var c=0; c<4; c++) counterBlock[15-c] = (b >>> c*8) & 0xff;
            for (var c=0; c<4; c++) counterBlock[15-c-4] = (b/0x100000000 >>> c*8);

            var cipherCntr = Algo.cipher(counterBlock, keySchedule);  // -- encrypt counter block --

            // block size is reduced on final block
            var blockLength = b<blockCount-1 ? blockSize : (plaintext.length-1)%blockSize+1;
            var cipherChar = new Array(blockLength);

            for (var i=0; i<blockLength; i++) {  // -- xor plaintext with ciphered counter char-by-char --
                cipherChar[i] = cipherCntr[i] ^ plaintext.charCodeAt(b*blockSize+i);
                cipherChar[i] = String.fromCharCode(cipherChar[i]);
            }
            ciphertxt[b] = cipherChar.join('');
        }

        // use Array.join() for better performance than repeated string appends
        var ciphertext = ctrTxt + ciphertxt.join('');
        ciphertext = ciphertext.base64Encode();

        return ciphertext;
    };

    encryptjs.decrypt = function(ciphertext, password, nBits) {
        var blockSize = 16;  // block size fixed at 16 bytes / 128 bits (Nb=4) for AES
        if (!(nBits==128 || nBits==192 || nBits==256)) return ''; // standard allows 128/192/256 bit keys
        ciphertext = String(ciphertext).base64Decode();
        password = String(password).utf8Encode();

        // use AES to encrypt password (mirroring encrypt routine)
        var nBytes = nBits/8;  // no bytes in key
        var pwBytes = new Array(nBytes);
        for (var i=0; i<nBytes; i++) {
            pwBytes[i] = isNaN(password.charCodeAt(i)) ? 0 : password.charCodeAt(i);
        }
        var key = Algo.cipher(pwBytes, Algo.keyExpansion(pwBytes));
        key = key.concat(key.slice(0, nBytes-16));  // expand key to 16/24/32 bytes long

        // recover nonce from 1st 8 bytes of ciphertext
        var counterBlock = new Array(8);
        var ctrTxt = ciphertext.slice(0, 8);
        for (var i=0; i<8; i++) counterBlock[i] = ctrTxt.charCodeAt(i);

        // generate key schedule
        var keySchedule = Algo.keyExpansion(key);

        // separate ciphertext into blocks (skipping past initial 8 bytes)
        var nBlocks = Math.ceil((ciphertext.length-8) / blockSize);
        var ct = new Array(nBlocks);
        for (var b=0; b<nBlocks; b++) ct[b] = ciphertext.slice(8+b*blockSize, 8+b*blockSize+blockSize);
        ciphertext = ct;  // ciphertext is now array of block-length strings

        // plaintext will get generated block-by-block into array of block-length strings
        var plaintxt = new Array(ciphertext.length);

        for (var b=0; b<nBlocks; b++) {
            // set counter (block #) in last 8 bytes of counter block (leaving nonce in 1st 8 bytes)
            for (var c=0; c<4; c++) counterBlock[15-c] = ((b) >>> c*8) & 0xff;
            for (var c=0; c<4; c++) counterBlock[15-c-4] = (((b+1)/0x100000000-1) >>> c*8) & 0xff;

            var cipherCntr = Algo.cipher(counterBlock, keySchedule);  // encrypt counter block

            var plaintxtByte = new Array(ciphertext[b].length);
            for (var i=0; i<ciphertext[b].length; i++) {
                // -- xor plaintxt with ciphered counter byte-by-byte --
                plaintxtByte[i] = cipherCntr[i] ^ ciphertext[b].charCodeAt(i);
                plaintxtByte[i] = String.fromCharCode(plaintxtByte[i]);
            }
            plaintxt[b] = plaintxtByte.join('');
        }

        // join array of blocks into single plaintext string
        var plaintext = plaintxt.join('');
        plaintext = plaintext.utf8Decode();  // decode from UTF8 back to Unicode multi-byte chars

        return plaintext;
    };

    encryptjs.getTextEncryptAndSaveToTextFile = function(filePath,password,nBits) {
        if (!rl) throw Error("Command line not supported on this platform");
        rl.question("Enter the text to be encrypted: ", function(answer) {
            // TODO: Log the answer in a database
            console.log("'"+answer+"' This text will be encrypted and stored in a text file 'encrypted.txt'");
           var cipherText=encryptjs.encrypt(answer,password,nBits);
            fs.writeFile(filePath,cipherText,function(){
                console.log("'encrypted.txt' File created in your local directory, if not present refresh your project");
            });
            rl.close();
        });
    };

    encryptjs.getTextEncryptAndSaveToJSONFile = function(filePath,password,nBits) {
        if (!rl) throw Error("Command line not supported on this platform");
        rl.question("Enter the text to be encrypted: ", function(answer) {
            // TODO: Log the answer in a database
            console.log("'"+answer+"' This text will be encrypted and stored in a text file 'encrypted.txt'");
            var cipherText=encryptjs.encrypt(answer,password,nBits);
            encryptjs.writeCipherTextToJSON(filePath,{EncryptedText:cipherText},function(){
                console.log("'encryptedText.JSON' File created in your local directory, if not present refresh your project");
            });
            rl.close();
        });
    };

    encryptjs.writeCipherTextToJSON=function(file, obj, options, callback) {
        if (callback == null) {
            callback = options;
            options = {}
        }

        var spaces = typeof options === 'object' && options !== null
            ? 'spaces' in options
            ? options.spaces : this.spaces
            : this.spaces;

        var str = '';
        try {
            str = JSON.stringify(obj, options ? options.replacer : null, spaces) + '\n'
        } catch (err) {
            if (callback) return callback(err, null)
        }

        fs.writeFile(file, str, options, callback)
    };

    if (typeof String.prototype.utf8Encode == 'undefined') {
        String.prototype.utf8Encode = function() {
            return unescape( encodeURIComponent( this ) );
        };
    }

    if (typeof String.prototype.utf8Decode == 'undefined') {
        String.prototype.utf8Decode = function() {
            try {
                return decodeURIComponent( escape( this ) );
            } catch (e) {
                return this; // invalid UTF-8? return as-is
            }
        };
    }

    if (typeof String.prototype.base64Encode == 'undefined') {
        String.prototype.base64Encode = function() {
            if (typeof btoa != 'undefined') return btoa(this); // browser
            if (typeof Buffer != 'undefined') return new Buffer(this, 'utf8').toString('base64'); // Node.js
            return base64_encode(this);
        };
    }

    if (typeof String.prototype.base64Decode == 'undefined') {
        String.prototype.base64Decode = function() {
            if (typeof atob != 'undefined') return atob(this); // browser
            if (typeof Buffer != 'undefined') return new Buffer(this, 'base64').toString('utf8'); // Node.js
            return base64_decode(this) ;
        };
    }

    encryptjs.init();

    return encryptjs;

});


function base64_encode(str) {
    var c1, c2, c3;
    var base64EncodeChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    var i = 0, len = str.length, string = "";

    while (i < len) {
        c1 = str.charCodeAt(i++) & 0xff;
        if (i == len) {
            string += base64EncodeChars.charAt(c1 >> 2);
            string += base64EncodeChars.charAt((c1 & 0x3) << 4);
            string += "==";
            break;
        }
        c2 = str.charCodeAt(i++);
        if (i == len) {
            string += base64EncodeChars.charAt(c1 >> 2);
            string += base64EncodeChars.charAt(((c1 & 0x3) << 4) | ((c2 & 0xF0) >> 4));
            string += base64EncodeChars.charAt((c2 & 0xF) << 2);
            string += "=";
            break;
        }
        c3 = str.charCodeAt(i++);
        string += base64EncodeChars.charAt(c1 >> 2);
        string += base64EncodeChars.charAt(((c1 & 0x3) << 4) | ((c2 & 0xF0) >> 4));
        string += base64EncodeChars.charAt(((c2 & 0xF) << 2) | ((c3 & 0xC0) >> 6));
        string += base64EncodeChars.charAt(c3 & 0x3F);
    }
    return string;
}
function base64_decode(str) {
    var c1, c2, c3, c4;
    var base64DecodeChars = [-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57,
        58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6,
        7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
        25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36,
        37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1,
        -1, -1];
    var i = 0, len = str.length, string = "";
    while (i < len) {
        do {
            c1 = base64DecodeChars[str.charCodeAt(i++) & 0xff];
        } while (
            i < len && c1 == -1
            );
        if (c1 == -1) break;
        do {
            c2 = base64DecodeChars[str.charCodeAt(i++) & 0xff];
        } while (
            i < len && c2 == -1
            );
        if (c2 == -1) break;
        string += String.fromCharCode((c1 << 2) | ((c2 & 0x30) >> 4));
        do {
            c3 = str.charCodeAt(i++) & 0xff;
            if (c3 == 61)
                return string;
            c3 = base64DecodeChars[c3];
        } while (
            i < len && c3 == -1
            );
        if (c3 == -1) break;
        string += String.fromCharCode(((c2 & 0XF) << 4) | ((c3 & 0x3C) >> 2));
        do {
            c4 = str.charCodeAt(i++) & 0xff;
            if (c4 == 61) return string;
            c4 = base64DecodeChars[c4];
        } while (
            i < len && c4 == -1
            );
        if (c4 == -1) break;
        string += String.fromCharCode(((c3 & 0x03) << 6) | c4);
    }
    return string;
}