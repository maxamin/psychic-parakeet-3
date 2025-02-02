﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Security.Cryptography;
using System.Runtime.Remoting.Metadata.W3cXsd2001;

namespace Puma.Prey.Common.Cryptography
{
    public class Random
    {
        public static byte[] GenerateCrytpoRandomBytes(int bytes)
        {
            byte[] numbers = new byte[bytes];
            System.Random random = new System.Random();
            random.NextBytes(numbers);
            return numbers;
        }
    }
}
