package org.xwiki.signedscripts;

/**
 * Defines which data is stored in a signature of a {@link SignedScript}.
 * <p>
 * NOTE: Do not change the order!
 * </p>
 * 
 * @version $Id$
 * @since 2.5
 */
public enum SignedScriptKey
{
    /** Author of the signed script. */
    AUTHOR("Author"),

    /** Root authority in the certificate chain, may be "Xwiki.org" or "Local". */
    AUTHORITY("Authority"),

    /** SHA-1 finger print of the certificate to use for verification. */
    FINGERPRINT("Fingerprint"),

    /** Version of XWiki used to sign. */
    XWIKIVERSION("XWikiVersion"),

    /** Creation date of the signature. */
    CREATEDON("CreatedOn"),

    /** Expiration date of the signature. Default is never. */
    EXPIRESON("ExpiresOn", ""),

    /** Document to bind the signed script to. Default is not bound. */
    DOCUMENT("Document", ""),

    /** List of comma-separated rights to grant to the signed script. Default is "programming". */
    ALLOWRIGHTS("AllowRights", "programming"),

    /** List of comma-separated method names allowed to run. Default is no restrictions. */
    ALLOWACTIONS("AllowActions", ""),

    /** Base64 encoded signature. */
    SIGNATURE("Signature"),

    /** Fake key name storing the script macro to run. */
    CODE("*Code Code*"),

    /** Invalid key name. */
    INVALID("", "");

    /** Name of this key. */
    private final String key;

    /** Flag to store if this key is optional. */
    private final boolean optional;

    /** Default value of the key if it is optional. */
    private final String defaultValue;

    /**
     * Initialize new mandatory key.
     * 
     * @param key name of this key
     */
    private SignedScriptKey(String key)
    {
        this.key = key;
        this.optional = false;
        this.defaultValue = null;
    }

    /**
     * Initialize new optional key with the given default value.
     * 
     * @param key name of this key
     * @param defaultValue default value if not set
     */
    private SignedScriptKey(String key, String defaultValue)
    {
        this.key = key;
        this.optional = true;
        this.defaultValue = defaultValue;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString()
    {
        return this.key;
    }

    /**
     * Check whether this key is optional.
     * 
     * @return true if this key is optional, false otherwise
     */
    public boolean isOptional()
    {
        return this.optional;
    }

    /**
     * Get the default value of this key or null if the key is mandatory.
     * 
     * @return the default value
     */
    public String getDefault()
    {
        return this.defaultValue;
    }

    /**
     * Get a {@link SignedScriptKey} enumeration by key name.
     * 
     * @param key key name
     * @return corresponding enumeration
     */
    public static SignedScriptKey byKey(String key)
    {
        for (SignedScriptKey k : values()) {
            if (k.toString().equals(key)) {
                return k;
            }
        }
        return INVALID;
    }
}
