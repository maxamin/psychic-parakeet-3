using System;

namespace TarpitCsharp.Utils
{
    public class Logger
    {
        public static void Error(string msg)
        {
            Log("Error", msg);
        }
        
        public static void Info(string msg)
        {
            Log("Info", msg);
        }

        private static void Log(string lvl, string msg)
        {
            Console.WriteLine($"[{lvl}]: {msg}");
        }
    }
}