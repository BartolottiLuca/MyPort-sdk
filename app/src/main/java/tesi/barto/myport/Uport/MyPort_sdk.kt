package tesi.barto.myport.Uport

import android.app.Application
import me.uport.sdk.Uport
import me.uport.sdk.fuelingservice.FuelTokenProvider

class MyPort_sdk : Application() {

    override fun onCreate() {
        super.onCreate()
        val config = Uport.Configuration().setApplicationContext(this).setFuelTokenProvider(FuelTokenProvider(this, "2p1yWKU8Ucd4vuHmYmc3fvcvTkYL11KXdjH"))
        Uport.initialize(config)
    }
}
