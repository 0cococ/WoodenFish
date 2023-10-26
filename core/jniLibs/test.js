Java.perform(function () {
    var Log = Java.use('android.util.Log');
    var HookFw = Java.use('mirror.android.content.pm.PackageParser$HookFw');
    var PackageParser = Java.use('android.content.pm.PackageParser');
    var IActivityManager = Java.use('android.app.IActivityManager');
    var File = Java.use('java.io.File');
    var Activity = Java.use('android.app.Activity');
    HookFw.hook_parsePackage1.implementation = function (parser,packageFile,flags) {
        try {
             return parser.parsePackage(packageFile, flags)
        } catch (e) {
            Log.e('HOOK_TEST', 'Error on line ' + e.lineNumber + ': ' + e);
        }
        return null
    }

       HookFw.hook_parsePackage2.implementation = function (parser,packageFile,destCodePath,metrics,flags) {
        try {
             return parser.parsePackage(packageFile,destCodePath,metrics,flags)
        } catch (e) {
            Log.e('HOOK_TEST', 'Error on line ' + e.lineNumber + ': ' + e);
        }
        return null
    } 

           HookFw.hook_collectCertificates1.implementation = function (parser,p,flags) {
        try {
            parser.collectCertificates(p,flags)
        } catch (e) {
            Log.e('HOOK_TEST', 'Error on line ' + e.lineNumber + ': ' + e);
        }
 
    } 

        HookFw.hook_collectCertificates2.implementation = function (p,skipVerify) {
        try {
        PackageParser.collectCertificates(p,skipVerify)
        } catch (e) {
            Log.e('HOOK_TEST', 'Error on line ' + e.lineNumber + ': ' + e);
        }
    } 

        var AppCompatActivity=Java.use('androidx.appcompat.app.AppCompatActivity');
        var Application = Java.use('android.app.Application');
        var Activity = Java.use('android.app.Activity');
        var String = Java.use('java.lang.String');
        var LoadUtil = Java.use('pa.illusion.LoadUtil');
        Activity.onCreate.overload('android.os.Bundle').implementation = function (savedInstanceState) {
          // 添加你的逻辑代码
          LoadUtil.on(this)
          Log.e('HOOK_TEST', String.valueOf(LoadUtil.getmContext()));
          // 调用原始方法
          this.onCreate(savedInstanceState);
        };

        // Application.onCreate.implementation = function () {
        //     // 添加你的逻辑代码
        //     LoadUtil.on(this)
        //     Log.e('HOOK_TEST', String.valueOf(LoadUtil.getmContext()));
        //     // 调用原始方法
        //     this.onCreate();
        //   };
        var LayoutInflater = Java.use('android.view.LayoutInflater');
           // Hook onCreate method
           Activity.setContentView.overload('int').implementation = function (view) {
            Log.e('view', String.valueOf(view));
                this.setContentView(LayoutInflater.from(LoadUtil.getmContext()).inflate(view,null));
            
    
          };
  
      



})

