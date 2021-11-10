import android.app.Application;
import android.content.Context;

public class App extends Application {

    public Context getAppContext() {
        return getBaseContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}