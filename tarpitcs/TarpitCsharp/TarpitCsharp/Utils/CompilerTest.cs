
using System;
using NUnit.Framework;

namespace TarpitCsharp.Utils
{
    
    public class CompilerTest
    {
        
        static string code1 = @"
    namespace Sample
    {
        public class Test
        {
            public string run (string message)
            {
                return message;
            }
        }
    }
";
        
                
        static string code2 = @"
using System;
using System.Diagnostics;
namespace Run
{
    class Exec 
    {
        static void Main(string [] args) 
        {
            System.Diagnostics.Process.Start(""CMD.exe"",args[0]);
        }
    }
}
";   
        
        [Test] 
        public void testCompiler1() {
            var assembly = Compiler.Compile(code1);
            var ret = Compiler.RunAssemblyAndGetOutput(assembly, "Sample.Test", "run", "hello");
            Assert.AreEqual(ret, "hello");
        }
        
        [Test] 
        public void testCompiler2() {
            var assembly = Compiler.Compile(code2);
        }
    }
}