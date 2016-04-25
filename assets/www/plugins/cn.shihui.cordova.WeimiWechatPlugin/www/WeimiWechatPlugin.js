cordova.define("cn.shihui.cordova.WeimiWechatPlugin.WeimiWechatPlugin", function(require, exports, module) {
var cordova = require('cordova');

var WeimiWechatPlugin = function() {};

WeimiWechatPlugin.prototype.login = function(success, error) {
    alert(11);
    cordova.exec(success, error, 'WeimiWechatPlugin', 'login', ["成功", "args2"]);
    alert(12);
};

var weimiWechatPlugin = new WeimiWechatPlugin();
module.exports = weimiWechatPlugin;

});
