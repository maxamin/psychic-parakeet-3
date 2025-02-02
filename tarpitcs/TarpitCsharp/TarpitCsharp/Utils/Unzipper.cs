using System;
using System.IO;
using System.IO.Compression;

namespace DefaultNamespace
{
    public class Unzipper
    {
        public static void unzipFile(string zipAbsolutePath, string destination)
        {
            ZipFile.ExtractToDirectory(zipAbsolutePath, destination);
        }
    }
}