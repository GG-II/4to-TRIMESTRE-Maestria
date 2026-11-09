/*
 * DIVA Android - Damn Insecure and Vulnerable App for Android
 *
 * Copyright 2016 © Payatu
 * Author: Aseem Jakhar aseem[at]payatu[dot]com
 * Websites: www.payatu.com  www.nullcon.net  www.hardwear.io www.null.co.in
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package jakhar.aseem.diva;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
    }

    public void checkout(View view) {
        EditText cctxt = (EditText) findViewById(R.id.ccText);
        try {
            // Assuming we do some HTTP requests credit card validation and processing
            //Everything seems fine and then we hit some unforseen error
            processCC(cctxt.getText().toString());
        } catch (RuntimeException re) {
            // CORRECCION: nunca registrar el PAN completo (ni otros datos sensibles) en el log
            // del sistema. Se registra unicamente una referencia no reversible (enmascarada)
            // suficiente para correlacionar el incidente, sin exponer el dato original.
            Log.e("diva-log", "Error while processing transaction. Card ref: " + maskCardNumber(cctxt.getText().toString()));
            Toast.makeText(this, "An error occured. Please try again later", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Devuelve una referencia no reversible del numero de tarjeta, mostrando unicamente
     * los ultimos 4 digitos (patron habitual en recibos/logs de procesadores de pago),
     * para que el log siga siendo util para soporte sin exponer el dato completo.
     */
    private String maskCardNumber(String ccNumber) {
        if (ccNumber == null || ccNumber.length() < 4) {
            return "****";
        }
        String lastFour = ccNumber.substring(ccNumber.length() - 4);
        return "****" + lastFour;
    }

    private void processCC(String ccstr) {
        // Do some important processing and throw if there is any error
        RuntimeException e = new RuntimeException();
        throw e;
    }
}
