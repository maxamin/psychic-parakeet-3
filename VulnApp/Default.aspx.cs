/*
$Revision$

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.
* Neither the name of the Nth Dimension nor the names of its contributors may
be used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

(c) Tim Brown, 2011
<mailto:timb@nth-dimension.org.uk>
<http://www.nth-dimension.org.uk/> / <http://www.machine.org.uk/>
*/

using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Data.SqlClient;

namespace VulnApp
{
    public partial class _Default : System.Web.UI.Page
    {
        private void Page_Load(object sender, System.EventArgs e)
        {
            if (Request.QueryString["error"] != null)
            {
                this.ErrorLabel.Text = Request.QueryString["error"];
            }
        }

        protected void LoginButton_Click(object sender, EventArgs e)
        {
            if ((this.UsernameTextBox.Text == "test") && (this.PasswordTextBox.Text == "test"))
            {
                Int16 ID = 0;
                Response.Cookies["user"]["id"] = ID.ToString();
                if (Request.QueryString["returnurl"] != null)
                {
                    Response.Redirect(Request.QueryString["returnurl"], true);
                }
                else
                {
                    Response.Redirect("Main.aspx", true);
                }
            }
            else
            {
                SqlConnection SQLConnection = new SqlConnection(VulnApp.Properties.Settings.Default.DatabaseConnectionString);
                SQLConnection.Open();
                SqlCommand SQLCommand = new SqlCommand("SELECT * from users WHERE username='" + this.UsernameTextBox.Text + "' AND password='" + this.PasswordTextBox.Text + "'", SQLConnection);
                SqlDataReader SQLDataReader = SQLCommand.ExecuteReader();
                if (SQLDataReader.HasRows)
                {
                    SQLDataReader.Read();
                    Int16 ID = SQLDataReader.GetInt16(0);
                    Response.Cookies["user"]["id"] = ID.ToString();
                    SQLConnection.Close();
                    SQLConnection.Dispose();
                    if (Request.QueryString["returnurl"] != null)
                    {
                        Response.Redirect(Request.QueryString["returnurl"], true);
                    }
                    else
                    {
                        Response.Redirect("Main.aspx", true);
                    }
                }
                else
                {
                    SQLConnection.Close();
                    SQLConnection.Dispose();
                }
            }
        }
    }
}
