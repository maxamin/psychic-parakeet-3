/* see copyright notice in VWSLang.h */

#ifndef VWSL_TOKEN_H
#define VWSL_TOKEN_H

#include "VWSLang.h"
#include <string>

struct vwslToken
{
    int              m_type;
    std::string*     m_name;
    vwslChar*        m_begin;
    vwslChar*        m_end;
    int              m_beginLine;
    int              m_endLine;

    union
    {
        vwslDchar     m_charValue;
        vwslUInt64    m_integerValue;
        vwslDouble    m_doubleValue;
    };

    std::string getRawString()
    {
        return std::string(m_begin, m_end);
    }
};

#endif //VWSL_TOKEN_H
