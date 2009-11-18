package org.xwiki.annotation.maintainment;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;
import org.xwiki.annotation.Annotation;
import org.xwiki.annotation.internal.maintainment.JavaDiffBasedAnnotationMaintainer;

/**
 * Test class for Java Diff based annotation Maintainer.
 * 
 * @version $Id$
 */
public class JavaDiffBasedAnnotationMaintainerTest extends JavaDiffBasedAnnotationMaintainer
{
    /**
     * @param state
     * @param id
     * @param offset
     * @param length
     * @return
     */
    public Annotation getFakeAnnotation(AnnotationState state, int id, int offset, int length)
    {
        return new Annotation(null, null, null, state, null, null, null, id, offset, length);
    }

    /**
     * Deletion after annotation.
     */
    @Test
    public void deletionAfter01()
    {
        previousContent = "This is not a simple sentence.";
        content = "This is a simple sentence.";
        currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 14, 6);
        recomputeProperties();
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    /**
     * Addition after annotation.
     */
    @Test
    public void additionAfter01()
    {
        content = "This is not a simple sentence.";
        previousContent = "This is a simple sentence.";
        currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 14, 6);
        recomputeProperties();
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    /**
     * Sticked deletion after annotation.
     */
    @Test
    public void deletionAfter02()
    {
        previousContent = "This is not a simple sentence.";
        content = "This is a simple sentence.";
        currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 12, 6);
        recomputeProperties();
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    /**
     * Sticked addition after annotation.
     */
    @Test
    public void additionAfter02()
    {
        content = "This is not a simple sentence.";
        previousContent = "This is a simple sentence.";
        currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 12, 6);
        recomputeProperties();
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    /**
     * Deletion in annotation.
     */
    @Test
    public void deletionIn01()
    {
        previousContent = "This is not a simple sentence.";
        content = "This is a simple sentence.";
        currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 10, 6);
        recomputeProperties();
        assertEquals(AnnotationState.ALTERED, currentAnnotation.getState());
    }

    /**
     * Addition in annotation.
     */
    @Test
    public void additionIn01()
    {
        content = "This is not a simple sentence.";
        previousContent = "This is a simple sentence.";
        currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 6, 6);
        recomputeProperties();
        assertEquals(AnnotationState.ALTERED, currentAnnotation.getState());
    }

    /**
     * Deletion in annotation.
     */
    @Test
    public void deletionIn02()
    {
        previousContent = "This is not a simple sentence.";
        content = "This is a simple sentence.";
        currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 8, 6);
        recomputeProperties();
        assertEquals(AnnotationState.ALTERED, currentAnnotation.getState());
    }

    /**
     * Addition in annotation.
     */
    @Test
    public void additionIn02()
    {
        content = "This is not a simple sentence.";
        previousContent = "This is a simple sentence.";
        currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 6, 6);
        recomputeProperties();
        assertEquals(AnnotationState.ALTERED, currentAnnotation.getState());
    }

    /**
     * Deletion in annotation.
     */
    @Test
    public void deletionIn03()
    {
        previousContent = "This is not a simple sentence.";
        content = "This is a simple sentence.";
        currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 7, 2);
        recomputeProperties();
        assertEquals(AnnotationState.ALTERED, currentAnnotation.getState());
    }

    /**
     * Addition in annotation.
     */
    @Test
    public void additionIn03()
    {
        content = "This is not a simple sentence.";
        previousContent = "This is a simple sentence.";
        currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 6, 2);
        recomputeProperties();
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    /**
     * Sticked deletion before annotation.
     */
    @Test
    public void deletionBefore01()
    {
        previousContent = "This is not a simple sentence.";
        content = "This is a simple sentence.";
        currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 7, 1);
        recomputeProperties();
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    /**
     * Sticked addition before annotation.
     */
    @Test
    public void additionBefore01()
    {
        content = "This is not a simple sentence.";
        previousContent = "This is a simple sentence.";
        currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 7, 1);
        recomputeProperties();
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    /**
     * Deletion before annotation.
     */
    @Test
    public void deletionBefore02()
    {
        previousContent = "This is not a simple sentence.";
        content = "This is a simple sentence.";
        currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 5, 3);
        recomputeProperties();
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    /**
     * Addition before annotation.
     */
    @Test
    public void additionBefore02()
    {
        content = "This is not a simple sentence.";
        previousContent = "This is a simple sentence.";
        currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 5, 3);
        recomputeProperties();
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    @Test
    public void regularDeletion01()
    {
        previousContent = "block2 block1 block1 block2";
        content = "block2 block1 block2";
        currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 19, 7);
        recomputeProperties();
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    @Test
    public void regularDeletion02()
    {
        previousContent = "another, limping, mimics the cripple who flew.__TEST__The Poet";
        content = "another, limping, mimics the cripple who flew.__The Poet bears a";
        currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 54, 8);
        recomputeProperties();
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    @Test
    public void regularAddition01()
    {
        previousContent = "block2 block1 block2";
        content = "block2 block1 block1 block2";
        currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 12, 7);
        recomputeProperties();
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }

    @Test
    public void regularAddition02()
    {
        previousContent = "another, limping, mimics the cripple who flew.__The Poet bears a";
        content = "another, limping, mimics the cripple who flew.__TEST__The Poet bears a";
        currentAnnotation = getFakeAnnotation(AnnotationState.SAFE, 0, 48, 6);
        recomputeProperties();
        assertEquals(AnnotationState.SAFE, currentAnnotation.getState());
    }
}
