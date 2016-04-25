cordova.define("TestPlugin.TestPlugin", function(require, exports, module) {
var cordova = require('cordova');

var TestPlugin = function() {};

TestPlugin.prototype.login = function(success, error) {
    cordova.exec(success, error, 'TestPlugin', 'action1', ["成功", "args2"]);
};

var TestPlugin = new TestPlugin();
module.exports = TestPlugin;

});
