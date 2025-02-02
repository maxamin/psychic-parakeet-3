using System.Collections.Generic;

namespace VideoDownloader.App.Extension
{
    public static class DictionaryExtension
    {
        public static TValue GetOrCreate<TKey, TValue>(this IDictionary<TKey, TValue> dict, TKey key) where TValue : new()
        {

            if (dict.TryGetValue(key, out TValue val))
            {
                return val;
            }
            val = new TValue();
            dict.Add(key, val);

            return val;
        }
    }
}
