/*
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *       Sun Microsystems, Inc. for Project JXTA."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact Project JXTA at http://www.jxta.org.
 *
 * 5. Products derived from this software may not be called "JXTA",
 *    nor may "JXTA" appear in their name, without prior written
 *    permission of Sun.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL SUN MICROSYSTEMS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Project JXTA.  For more
 * information on Project JXTA, please see
 * <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache Foundation.
 *
 * $Id$
 */

package org.xwoot.jxta.mock;

import java.io.IOException;
import java.util.Iterator;
import java.net.URI;
import net.jxta.access.AccessService;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.Element;
import net.jxta.endpoint.EndpointService;
import net.jxta.exception.ServiceNotFoundException;
import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ProtocolNotSupportedException;
import net.jxta.id.ID;
import net.jxta.membership.MembershipService;
import net.jxta.peer.PeerID;
import net.jxta.peer.PeerInfoService;
import net.jxta.pipe.PipeService;
import net.jxta.platform.JxtaLoader;
import net.jxta.platform.Module;
import net.jxta.platform.ModuleSpecID;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.ConfigParams;
import net.jxta.rendezvous.RendezVousService;
import net.jxta.resolver.ResolverService;
import net.jxta.service.Service;

import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;

/**
 * Simulate a JXTA peer group, for use by the SimP2PFace.java JXTA simulator. Most of it is stubbed out. It's just a
 * container for the group and peer advertisements.
 */
public class SimPeerGroup implements PeerGroup
{

    /** Group adv for this peer group instance. */
    private PeerGroupAdvertisement groupAdv;

    /** Peer adv for this peer group instance. */
    private PeerAdvertisement peerAdv;

    /** If a peer is rendezvous for this group. */
    private boolean isRendezvous;

    /**
     * Constructor.
     * 
     * @param groupAdv the group advertisement to assign to this peer group instance.
     * @param peerAdv the peer advertisement to assign to this peer group instance.
     */
    public SimPeerGroup(PeerGroupAdvertisement groupAdv, PeerAdvertisement peerAdv)
    {

        this.groupAdv = groupAdv;
        this.peerAdv = peerAdv;
    }

    /**
     * Returns the Thread Group in which threads for this peer group will live.
     * 
     * @return ThreadGroup
     */
    public ThreadGroup getHomeThreadGroup()
    {
        return null;
    }

    /**
     * Returns the loader for this group.
     * 
     * @return JxtaLoader The loader
     */
    public JxtaLoader getLoader()
    {
        return null;
    }

    /**
     * Returns the whether the group member is a Rendezvous peer for the group.
     * 
     * @return boolean true if the peer is a rendezvous for the group.
     */
    public boolean isRendezvous()
    {
        return isRendezvous;
    }

    /**
     * Ask a group for its group advertisement.
     * 
     * @return PeerGroupAdvertisement this Group's advertisement
     */
    public PeerGroupAdvertisement getPeerGroupAdvertisement()
    {
        return groupAdv;
    }

    /**
     * Ask a group for its peer advertisement on this peer.
     * 
     * @return PeerAdvertisement this Peer X Group advertisement
     */
    public PeerAdvertisement getPeerAdvertisement()
    {
        return peerAdv;
    }

    /**
     * Lookup for a service by name.
     * 
     * @param name the service identifier.
     * @return Service, the Service registered by that name
     * @exception ServiceNotFoundException could not find the service requested
     */
    public Service lookupService(ID name) throws ServiceNotFoundException
    {
        return null;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public boolean compatible(Element compat)
    {
        return false;
    }

    /**
     * Load a module from a ModuleImplAdv. Compatibility is checked and load is attempted. If compatible and loaded
     * successfuly, the resulting Module is initialized and returned. In most cases the other loadModule() method should
     * be preferred, since unlike this one, it will seek many compatible implementation advertisements and try them all
     * until one works. The home group of the new module (its parent group if the new module is a group) will be this
     * group.
     * 
     * @param assignedID Id to be assigned to that module (usually its ClassID).
     * @param impl An implementation advertisement for that module.
     * @exception ProtocolNotSupportedException The module is a protocol and is disabled per the peer's configuration.
     * @exception PeerGroupException The module could not be loaded or initialized
     * @return Module the module loaded and initialized.
     */
    public Module loadModule(ID assignedID, Advertisement impl) throws ProtocolNotSupportedException,
        PeerGroupException
    {
        return null;
    }

    /**
     * Load a module from a spec id. Advertisement is sought, compatibility is checked on all candidates and load is
     * attempted. The first one that is compatible and loads successfuly is initialized and returned.
     * 
     * @param assignedID Id to be assigned to that module (usually its ClassID).
     * @param specID The specID of this module.
     * @param where May be one of: Here, FromParent, or Both, meaning that the implementation advertisement will be
     *            searched in this group, its parent or both. As a general guideline, the implementation advertisements
     *            of a group should be searched in its propsective parent (that is Here), the implementation
     *            advertisements of a group standard service should be searched in the same group than where this
     *            group's advertisement was found (that is, FromParent), while applications may be sought more freely
     *            (Both).
     * @return Module the new module, or null if no usuable implementation was found.
     */
    public Module loadModule(ID assignedID, ModuleSpecID specID, int where)
    {
        return null;
    }

    /**
     * Force publication of this group if hasn't been done already. Calling this routine is only usefull if the group is
     * being created from scratch and the PeerGroup advertisement has not been created beforehand. In such a case, the
     * group has never been named or described. Therefore this information has to be supplied here. If this group has
     * already been published, this method does nothing.
     * 
     * @param name The name of this group.
     * @param description The description of this group.
     * @exception IOException The publication could not be accomplished because of a network or storage failure.
     */
    public void publishGroup(String name, String description) throws IOException
    {
    }

    /*
     * Valuable application helpers: Various methods to instantiate groups.
     */

    /**
     * Instantiate a group from its given advertisement Use this when a published implAdv for the groupSubclass can be
     * discovered. The pgAdv itself may be all new and unpublished. Therefore, the two typical uses of this routine are:
     * - Creating an all new group with a new ID while using an existing and published implementation. (Possibly a new
     * one published for that purpose). The information should first be gathered in a new PeerGroupAdvertisement which
     * is then passed to this method. - Instantiating a group which advertisement has already been discovered (therefore
     * there is no need to find it by groupID again). To create a group from a known implAdv, use newGroup(gid, impladv,
     * name, description);
     * 
     * @param pgAdv The advertisement of that group.
     * @return PeerGroup the initialized (but not started) peergroup.
     * @exception PeerGroupException The group could ne be instantated.
     */
    public PeerGroup newGroup(Advertisement pgAdv) throws PeerGroupException
    {
        return null;
    }

    /**
     * Convenience method, instantiate a group from its elementary pieces and publish the corresponding
     * PeerGroupAdvertisement. The pieces are: the groups implementation adv, the group id, the name and description.
     * The typical use of this routine is creating a whole new group based on a newly created and possibly unpublished
     * implementation adv. This is equivalent to either: newGrp = thisGroup.loadModule(gid, impl);
     * newGrp.publishGroup(name, description); or, but only if the implementation adv has been published: newPGAdv =
     * AdvertisementFactory.newAdvertisement( new MimeMediaType("text", "xml")),
     * PeerGroupAdvertisement.getAdvertisementType()); newPGAdv.setPeerGroupID(gid);
     * newPGAdv.setModuleSpecID(impl.getModuleSpecID()); newPGAdv.setName(name); newPGAdv.setDescription(description);
     * newGrp = thisGroup.newGroup(newPGAdv);
     * 
     * @param gid The ID of that group.
     * @param impl The advertisement of the implementation to be used.
     * @param name The name of the group.
     * @param description A description of this group.
     * @return PeerGroup the initialized (but not started) peergroup.
     * @exception PeerGroupException The group could ne be instantated.
     */
    public PeerGroup newGroup(PeerGroupID gid, Advertisement impl, String name, String description)
        throws PeerGroupException
    {
        return null;
    }

    /**
     * Instantiate a group from its groupID only. Use this when using a group that has already been published and
     * discovered. The typical uses of this routine are therefore: - instantiating a group which is assumed to exist and
     * which GID is known. - creating a new group using an already published advertisement. Typically published for that
     * purpose. All other implied advertisements must also have been published beforehand if they did not exist. To
     * create a group from a known implAdv, just use loadModule(gid, implAdv, config) or even: grp = new
     * GroupSubClass(); grp.init(parentGroup, gid, impladv, config); then, REMEMBER TO PUBLISH THE GROUP IF IT IS ALL
     * NEW.
     * 
     * @param gid the groupID.
     * @return PeerGroup the initialized (but not started) peergroup.
     * @exception PeerGroupException The group could ne be instantated.
     */
    public PeerGroup newGroup(PeerGroupID gid) throws PeerGroupException
    {
        return null;
    }

    /*
     * shortcuts to the well-known services, in order to avoid calls to lookup.
     */

    /**
     * @return RendezVousService an object implementing the RendezVousService service for this group.
     */
    public RendezVousService getRendezVousService()
    {
        return null;
    }

    /**
     * @return EndpointService an object implementing the EndpointService service for this group.
     */
    public EndpointService getEndpointService()
    {
        return null;
    }

    /**
     * @return ResolverService an object implementing the ResolverService service for this group.
     */
    public ResolverService getResolverService()
    {
        return null;
    }

    /**
     * @return DiscoveryService an object implementing the DiscoveryService service for this group.
     */
    public DiscoveryService getDiscoveryService()
    {
        return null;
    }

    /**
     * @return PeerInfoService an object implementing the PeerInfoService service for this group.
     */
    public PeerInfoService getPeerInfoService()
    {
        return null;
    }

    /**
     * @return MembershipService an object implementing the MembershipService service for this group.
     */
    public MembershipService getMembershipService()
    {
        return null;
    }

    /**
     * @return PipeService an object implementing the PipeService service for this group.
     */
    public PipeService getPipeService()
    {
        return null;
    }

    /*
     * A few convenience methods. This information is available from the peer and peergroup advertisement.
     */

    /**
     * Tell the ID of this group.
     * 
     * @return PeerGroupId this Group's ID
     */
    public PeerGroupID getPeerGroupID()
    {
        return groupAdv.getPeerGroupID();
    }

    /**
     * Tell the ID of this peer in this group.
     * 
     * @return PeerId this peer's ID
     */
    public PeerID getPeerID()
    {
        return peerAdv.getPeerID();
    }

    /**
     * Tell the Name of this group.
     * 
     * @return String this group's name
     */
    public String getPeerGroupName()
    {
        return groupAdv.getName();
    }

    /**
     * Tell the Name of this peer in this group.
     * 
     * @return String this peer's name
     */
    public String getPeerName()
    {
        return peerAdv.getName();
    }

    /**
     * Returns the config advertisment for this peer in this group (if any).
     * 
     * @return PeerGroup the parent peergroup.
     */
    public PeerGroup getParentGroup()
    {
        return null;
    }

    /** {@inheritDoc} */
    public ModuleImplAdvertisement getAllPurposePeerGroupImplAdvertisement() throws Exception
    {
        return null;
    }

    /* Service interface support. */

    /**
     * Service objects are not manipulated directly to protect usage of the service. A Service interface is returned to
     * access the service methods.
     * 
     * @return Service public interface of the service
     * @since JXTA 1.0
     */
    public Service getInterface()
    {
        return null;
    }

    /**
     * Returns the advertisment for this service.
     * 
     * @return Advertisement the advertisement.
     * @since JXTA 1.0
     */
    public Advertisement getImplAdvertisement()
    {
        return null;
    }

    /* Module interface support. */

    /**
     * Initialize the module, passing it its peer group and advertisement.
     * 
     * @param group The PeerGroup from which this Module can obtain services. If this module is a service, this is also
     *            the PeerGroup of which this module is a service.
     * @param assignedID Identity of Module within group. modules can use it as a the root of their namespace to create
     *            names that are unique within the group but predictible by the same module on another peer. This is
     *            normaly the ModuleClassID which is also the name under which the module is known by other modules. For
     *            a group it is the PeerGroupID itself. The parameters of a service, in the Peer configuration, are
     *            indexed by the assignedID of that service, and a Service must publish its run-time parameters in the
     *            Peer Advertisement under its assigned ID.
     * @param implAdv The implementation advertisement for this Module.
     * @exception PeerGroupException This module failed to initialize.
     * @since JXTA 1.0
     */
    public void init(PeerGroup group, ID assignedID, Advertisement implAdv) throws PeerGroupException
    {
    }

    /**
     * Some Modules will wait for start() being called, before proceeding beyond a certain point. That's also the
     * opportunity to supply harbitrary arguments (mostly to applications). Note: the name of this method is historical
     * and no-longer adequate.
     * 
     * @param args An array of Strings forming the parameters for this Module.
     * @return int status indication. By convention 0 means that this Module started succesfully.
     */
    public int startApp(String[] args)
    {
        return 0;
    }

    /**
     * One can ask a Module to stop. The Module cannot be forced to comply, but in the future we might be able to deny
     * it access to anything after some timeout. Note: the name of this method is no-longer adequate.
     */
    public void stopApp()
    {
    }

    /** {@inheritDoc} */
    public void unref()
    {
    }

    /** {@inheritDoc} */
    public PeerGroup getWeakInterface()
    {
        return this;
    }

    /** {@inheritDoc} */
    public ConfigParams getConfigAdvertisement()
    {
        return null;
    }

    /** {@inheritDoc} */
    public AccessService getAccessService()
    {
        return null;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public Iterator getRoleMap(ID name)
    {
        return null;
    }

    /** {@inheritDoc} */
    public Service lookupService(ID name, int ignoredForNow) throws ServiceNotFoundException
    {
        throw new ServiceNotFoundException("Not implemented");
    }

    /** {@inheritDoc} */
    public URI getStoreHome()
    {
        return null;
    }
}
