cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
    {
        "file": "plugins/TestPlugin/www/TestPlugin.js",
        "id": "TestPlugin.TestPlugin",
        "clobbers": [
            "TestPlugin"
        ]
    },
    {
        "file": "plugins/cn.shihui.cordova.WeimiWechatPlugin/www/WeimiWechatPlugin.js",
        "id": "cn.shihui.cordova.WeimiWechatPlugin.WeimiWechatPlugin",
        "clobbers": [
            "WeimiWechatPlugin"
        ]
    }
];
module.exports.metadata = 
// TOP OF METADATA
{
    "TestPlugin": "0.0.1",
    "cn.shihui.cordova.WeimiWechatPlugin": "0.0.1"
};
// BOTTOM OF METADATA
});