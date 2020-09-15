package ilapi.iptables_app

import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.execButton
import kotlinx.android.synthetic.main.activity_main.inputEditText
import kotlinx.android.synthetic.main.activity_main.outputTextView

class MainActivity : AppCompatActivity() {

    private val handler = Handler()

    private var workerThread: WorkerThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        execButton.setOnClickListener {
            val currentWorkerThread = workerThread
            val inputText = inputEditText.text.toString()
            if (currentWorkerThread == null || !currentWorkerThread.isAlive) {
                val newWorkerThread = object : WorkerThread() {

                    override fun run() {
                        try {
                            val bufferedReader = Runtime.getRuntime().exec(inputText).inputStream.bufferedReader()
                            val processOutput = bufferedReader.readText()
                            bufferedReader.close()
                            handler.post {
                                outputTextView.text = processOutput
                            }
                        } catch (e: InterruptedException) {
                            handler.post {
                                Toast.makeText(this@MainActivity, getString(R.string.process_execution_interrupted), Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                newWorkerThread.start()
                workerThread = newWorkerThread
            } else {
                Toast.makeText(this, getString(R.string.busy), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        workerThread?.apply {
            terminate()
            join()
        }
    }

    open class WorkerThread : Thread() {

        @Volatile
        protected var isTerminationRequested = false

        fun terminate() {
            isTerminationRequested = true
        }
    }
}