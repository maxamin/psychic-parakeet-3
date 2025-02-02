using System;
using System.IO;
using System.Linq;
using System.Reflection;
using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp;

namespace TarpitCsharp.Utils
{
    public class Compiler
    {
        public static Assembly Compile(string clazz)
        {
            var dd = typeof(Enumerable).GetTypeInfo().Assembly.Location;
            var coreDir = Directory.GetParent(dd);
            var assemblyName = Path.GetRandomFileName();
            MetadataReference[] references = {
                MetadataReference.CreateFromFile(typeof(object).Assembly.Location),
                MetadataReference.CreateFromFile(typeof(Enumerable).Assembly.Location),
                MetadataReference.CreateFromFile(typeof(System.Diagnostics.Process).Assembly.Location),
                MetadataReference.CreateFromFile(typeof(System.ComponentModel.Component).Assembly.Location),
                MetadataReference.CreateFromFile(typeof(Object).GetTypeInfo().Assembly.Location),
                MetadataReference.CreateFromFile(typeof(Uri).GetTypeInfo().Assembly.Location),
                MetadataReference.CreateFromFile(coreDir.FullName + Path.DirectorySeparatorChar + "mscorlib.dll"),
                MetadataReference.CreateFromFile(coreDir.FullName + Path.DirectorySeparatorChar + "System.Runtime.dll")
            };

            var compilation = CSharpCompilation.Create(
                assemblyName,
                new[] { CSharpSyntaxTree.ParseText(clazz) },
                references,
                new CSharpCompilationOptions(OutputKind.DynamicallyLinkedLibrary));
           
            using (var ms = new MemoryStream())
            {
                var result = compilation.Emit(ms);

                if (!result.Success)
                {
                    var failures = result.Diagnostics.Where(diagnostic => 
                        diagnostic.IsWarningAsError || 
                        diagnostic.Severity == DiagnosticSeverity.Error);

                    foreach (var diagnostic in failures)
                    {
                        Console.Error.WriteLine("{0}: {1}", diagnostic.Id, diagnostic.GetMessage());
                    }
                }
                else
                {
                    ms.Seek(0, SeekOrigin.Begin);
                    return Assembly.Load(ms.ToArray());
                }
            }

            return null;
        }

        public static object loadClass(Assembly ass, string stype)
        {
            var type = ass.GetType(stype);
            return Activator.CreateInstance(type);
        }

        public static string RunAssemblyAndGetOutput(Assembly ass, string stype, string member, string input)
        {
            return (string)InvokeMethod(ass,stype,member,input);
        }
        
        public static object InvokeMethod(Assembly ass, string stype, string member, string input)
        {
            var type = ass.GetType(stype);
            var obj = Activator.CreateInstance(type);
            return type.InvokeMember(member,
                BindingFlags.Default | BindingFlags.InvokeMethod,
                null,
                obj,
                new object[] { input });
        }
    }
}