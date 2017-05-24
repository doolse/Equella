package com.tle.admin.security.tree.model;

import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import com.tle.common.security.PrivilegeTree.Node;

/**
 * @author Nicholas Read
 */
public abstract class AbstractLeafNode<T> implements SecurityTreeNode
{
	private final T entity;
	private final Node privNode;

	private SecurityTreeNode parent;

	public AbstractLeafNode(T entity, Node privNode)
	{
		this.entity = entity;
		this.privNode = privNode;
	}

	public T getEntity()
	{
		return entity;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.admin.security.tree.model.SecurityTreeNode#getPrivilegeNode()
	 */
	@Override
	public Node getPrivilegeNode()
	{
		return privNode;
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.admin.security.tree.model.SecurityTreeNode#getTargetObject()
	 */
	@Override
	public Object getTargetObject()
	{
		return entity;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.admin.security.tree.SecurityTreeNode#setParent(com.tle.admin.
	 * security.tree.SecurityTreeNode)
	 */
	@Override
	public void setParent(SecurityTreeNode parent)
	{
		this.parent = parent;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getChildAt(int)
	 */
	@Override
	public TreeNode getChildAt(int childIndex)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getChildCount()
	 */
	@Override
	public int getChildCount()
	{
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getParent()
	 */
	@Override
	public TreeNode getParent()
	{
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
	 */
	@Override
	public int getIndex(TreeNode node)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getAllowsChildren()
	 */
	@Override
	public boolean getAllowsChildren()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#isLeaf()
	 */
	@Override
	public boolean isLeaf()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#children()
	 */
	@Override
	public Enumeration<?> children()
	{
		throw new UnsupportedOperationException();
	}
}