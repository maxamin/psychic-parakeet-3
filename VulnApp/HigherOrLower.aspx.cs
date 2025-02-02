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
using VulnApp.VulnServiceStub;

namespace VulnApp
{
    public partial class HigherOrLower : System.Web.UI.Page
    {
        protected void Page_Load(object sender, EventArgs e)
        {
            Int16 ID = -1;
            if (Request.Cookies["user"] != null)
            {
                if (Request.Cookies["user"]["id"] != null)
                {
                    ID = Int16.Parse(Request.Cookies["user"]["id"]);
                }
            }
            SqlConnection SQLConnection = new SqlConnection(VulnApp.Properties.Settings.Default.DatabaseConnectionString);
            SQLConnection.Open(); 
            SqlCommand SQLCommand = new SqlCommand("SELECT * from users WHERE id=" + ID.ToString(), SQLConnection);
            SqlDataReader SQLDataReader = SQLCommand.ExecuteReader();
            if (SQLDataReader.HasRows)
            {
                if (Session["higherorlowerrandom"] == null)
                {
                    Random HigherOrLowerRandom = new Random(ID);
                    Session["higherorlowerrandom"] = HigherOrLowerRandom;
                    this.NumberLabel.Text = ((Random)Session["higherorlowerrandom"]).Next().ToString();
                }
                VulnService VulnService = new VulnService();
                this.ScoreLabel.Text = VulnService.GetScore(ID.ToString()) + " points";
            }
            else
            {
                ID = -1;
                Response.Cookies["user"]["id"] = ID.ToString();
                SQLConnection.Close();
                SQLConnection.Dispose();
                Response.Redirect("Default.aspx?returnurl=HigherOrLower.aspx&error=Please+log+in", true);
            }
            SQLConnection.Close();
            SQLConnection.Dispose();
        }

        protected void HigherButton_Click(object sender, EventArgs e)
        {
            Int16 ID = -1;
            if (Request.Cookies["user"] != null)
            {
                if (Request.Cookies["user"]["id"] != null)
                {
                    ID = Int16.Parse(Request.Cookies["user"]["id"]);
                }
            }
            Int32 CurrentNumber = Int32.Parse(this.NumberLabel.Text);
            this.NumberLabel.Text = ((Random)Session["higherorlowerrandom"]).Next().ToString();
            SqlConnection SQLConnection = new SqlConnection(VulnApp.Properties.Settings.Default.DatabaseConnectionString);
            SQLConnection.Open();
            SqlCommand SQLCommand = new SqlCommand("SELECT * from users WHERE id=" + ID.ToString(), SQLConnection);
            SqlDataReader SQLDataReader = SQLCommand.ExecuteReader();
            if (SQLDataReader.HasRows)
            {
                SQLDataReader.Read();
                Int64 CurrentScore = SQLDataReader.GetInt64(5);
                if (CurrentNumber < Int32.Parse(this.NumberLabel.Text))
                {
                    SQLConnection.Close();
                    SQLConnection.Open();
                    SQLCommand = new SqlCommand("UPDATE users SET score=" + (CurrentScore + 100) + " WHERE id=" + ID.ToString(), SQLConnection);
                    SQLCommand.ExecuteNonQuery();
                    VulnService VulnService = new VulnService();
                    this.ScoreLabel.Text = VulnService.GetScore(ID.ToString()) + " points; You said Higher (Old: " + CurrentNumber.ToString() + " < New: " + this.NumberLabel.Text + ") #winning";
                }
                else
                {
                    SQLConnection.Close();
                    SQLConnection.Open();
                    SQLCommand = new SqlCommand("UPDATE users SET score=" + (CurrentScore - 100) + " WHERE id=" + ID.ToString(), SQLConnection);
                    SQLCommand.ExecuteNonQuery();
                    VulnService VulnService = new VulnService();
                    this.ScoreLabel.Text = VulnService.GetScore(ID.ToString()) + " points; You said Higher (Old: " + CurrentNumber.ToString() + " > New: " + this.NumberLabel.Text + ") :(";
                }
            }
            else
            {
                ID = -1;
                Response.Cookies["user"]["id"] = ID.ToString();
                SQLConnection.Close();
                SQLConnection.Dispose();
                Response.Redirect("Default.aspx?returnurl=HigherOrLower.aspx&error=Please+log+in", true);
            }
            SQLConnection.Close();
            SQLConnection.Dispose();
        }

        protected void LowerButton_Click(object sender, EventArgs e)
        {
            Int16 ID = -1;
            if (Request.Cookies["user"] != null)
            {
                if (Request.Cookies["user"]["id"] != null)
                {
                    ID = Int16.Parse(Request.Cookies["user"]["id"]);
                }
            }
            Int32 CurrentNumber = Int32.Parse(this.NumberLabel.Text);
            this.NumberLabel.Text = ((Random)Session["higherorlowerrandom"]).Next().ToString();
            SqlConnection SQLConnection = new SqlConnection(VulnApp.Properties.Settings.Default.DatabaseConnectionString);
            SQLConnection.Open();
            SqlCommand SQLCommand = new SqlCommand("SELECT * from users WHERE id=" + ID.ToString(), SQLConnection);
            SqlDataReader SQLDataReader = SQLCommand.ExecuteReader();
            if (SQLDataReader.HasRows)
            {
                SQLDataReader.Read();
                Int64 CurrentScore = SQLDataReader.GetInt64(5);
                if (CurrentNumber > Int32.Parse(this.NumberLabel.Text))
                {
                    SQLConnection.Close();
                    SQLConnection.Open();
                    SQLCommand = new SqlCommand("UPDATE users SET score=" + (CurrentScore + 100) + " WHERE id=" + ID.ToString(), SQLConnection);
                    SQLCommand.ExecuteNonQuery();
                    VulnService VulnService = new VulnService();
                    this.ScoreLabel.Text = VulnService.GetScore(ID.ToString()) + " points; You said Lower (Old: " + CurrentNumber.ToString() + " > New: " + this.NumberLabel.Text + ") #winning"; 
                }
                else
                {
                    SQLConnection.Close();
                    SQLConnection.Open();
                    SQLCommand = new SqlCommand("UPDATE users SET score=" + (CurrentScore - 100) + " WHERE id=" + ID.ToString(), SQLConnection);
                    SQLCommand.ExecuteNonQuery();
                    VulnService VulnService = new VulnService();
                    this.ScoreLabel.Text = VulnService.GetScore(ID.ToString()) + " points; You said Lower (Old: " + CurrentNumber.ToString() + " < New: " + this.NumberLabel.Text + ") :("; 
                }
            }
            else
            {
                ID = -1;
                Response.Cookies["user"]["id"] = ID.ToString();
                SQLConnection.Close();
                SQLConnection.Dispose();
                Response.Redirect("Default.aspx?returnurl=HigherOrLower.aspx&error=Please+log+in", true);
            }
            SQLConnection.Close();
            SQLConnection.Dispose();
        }
    }
}
