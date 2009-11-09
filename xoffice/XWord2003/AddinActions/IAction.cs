using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace XWord2003.AddinActions
{
    /// <summary>
    /// Interface for addin actions performers.
    /// </summary>
    /// <typeparam name="T">Returned type.</typeparam>
    public interface IAction<T>
    {
        void Perform();
        T GetResults();
    }
}
