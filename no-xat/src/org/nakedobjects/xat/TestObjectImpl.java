package org.nakedobjects.xat;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.Association;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.reflect.NakedClassException;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.security.SecurityContext;
import org.nakedobjects.security.Session;

import java.util.Enumeration;
import java.util.Hashtable;

import junit.framework.Assert;


public class TestObjectImpl extends AbstractTestObject implements TestObject {
    private SecurityContext context;
    private Hashtable fields;

    public TestObjectImpl(SecurityContext context, NakedObject object) {
        this(context, object, new Hashtable());
    }

    public TestObjectImpl(SecurityContext context, NakedObject object, Hashtable viewCache) {
        this.context = context;
        setForObject(object);
        fields = new Hashtable();

        if (object != null) {
            Field[] a = object.getNakedClass().getFields();

            for (int i = 0; i < a.length; i++) {
                Field att = a[i];

                if (att instanceof Association) {
                    NakedObject attribute = (NakedObject) a[i].get(object);

                    TestObject associatedView = null;

                    if (null != attribute) { // if object is not null,
                        // use
                        // the view cache
                        associatedView = (TestObject) viewCache.get(attribute);
                    }

                    if (null == associatedView) {
                        associatedView = factory().createTestObject(context, attribute, viewCache);
                        // this puts it into the viewCache
                    }

                    fields.put(a[i].getName(), associatedView);
                } else {
                    fields.put(att.getName(), factory().createTestValue((NakedValue) att.get(object)));
                }
            }
        }
    }

    /** @deprecated */
    public void assertCantInvokeAction(String name) {
        assertActionUnusable(name);
    }

    /** @deprecated */
    public void assertCantInvokeAction(String name, TestObject parameter) {
        assertActionUnusable(name, parameter);
    }

    public void assertEmpty(String fieldName) {
        assertEmpty(null, fieldName);
    }

    public void assertEmpty(String message, String fieldName) {
        Field field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
         
        TestNaked actual = getField(fieldName);
        Object object = actual.getForObject();

        // TODO refactor to remove redundancy
        if (object instanceof NakedValue) {
            NakedValue value = ((NakedValue) object);
            if (!value.isEmpty()) {
                Assert.fail(expected(message) + " " + fieldName + " to be empty, but contained " + value.title().toString());
            }
        } else {
            if (object != null) {
                Assert.fail(expected(message) + " " + fieldName + " to be empty, but contained "
                        + ((NakedObject) object).title().toString());
            }
        }
    }

    public void assertFieldContains(String fieldName, NakedValue expectedValue) {
        assertFieldContains(null, fieldName, expectedValue);
    }

    /**
     * Check that the specified field contains the expected value. If it does not contain the
     * expected value the test fails.
     * 
     * @group assert
     */
    public void assertFieldContains(String fieldName, String expectedValue) {
        assertFieldContains(null, fieldName, expectedValue);
    }

    /**
     * Check that the specified field has the same value as the specified NakedValue. If it differs
     * the test fails. A note is added to the documentation to explain that the specified field now
     * has a specific value.
     *  
     */
    public void assertFieldContains(String message, String fieldName, NakedValue expectedValue) {
        Field field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
         
        TestNaked actual = getField(fieldName);
        NakedValue actualValue = ((NakedValue) actual.getForObject());

        if (!actualValue.isSameAs(expectedValue)) {
            Assert.fail(expected(message) + " value of " + expectedValue + " but got " + actualValue);
        }
    }

    /**
     * Check that the specified field contains the expected value. If it does not contain the
     * expected value the test fails.
     * 
     * @param message
     *                   text to add to the failure message, which is displayed after a failure.
     * @group assert
     */
    public void assertFieldContains(String message, String fieldName, String expectedValue) {
        Field field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
         
        Naked object = getField(fieldName).getForObject();

        if (object instanceof InternalCollection) {
            InternalCollection collection = (InternalCollection) object;
            for (int i = 0; i < collection.size(); i++) {
                NakedObject element = collection.elementAt(i);
                if (element.title().toString().equals(expectedValue)) { return; }
            }
            Assert.fail(expected(message) + " object " + expectedValue + " but could not find it in the internal collection");
        } else {
            String actualValue = object.title().toString();

            if (!actualValue.equals(expectedValue)) {
                Assert.fail(expected(message) + " value " + expectedValue + " but got " + actualValue);
            }
        }
    }

    /**
     * Check that the specified field contains the expected object (as represented by the specifed
     * view. If it does not contain the expected object the test fails.
     * 
     * @param message
     *                   text to add to the failure message, which is displayed after a failure.
     * @group assert
     */
    public void assertFieldContains(String message, String fieldName, TestObject expectedView) {
        Field field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
         
        Naked actualObject = getField(fieldName).getForObject();

        if (expectedView == null) {
            if (actualObject instanceof InternalCollection) {
                int size = ((InternalCollection) actualObject).size();

                if (size > 0) {
                    Assert.fail(expected(message) + " " + fieldName + " collection to contain zero elements, but found " + size);
                }
            } else if (actualObject != null) {
                Assert.fail(expected(message) + " an empty field, but found " + actualObject);
            }
        } else {
            Naked expectedObject = expectedView.getForObject();

            if (actualObject == null) {
                if (expectedObject != null) Assert.fail(expected(message) + expectedObject + "  but found an empty field");
            } else if (actualObject instanceof InternalCollection) {
                if (!((InternalCollection) actualObject).contains((NakedObject) expectedObject)) {
                    Assert.fail(expected(message) + " " + fieldName + " collection to contain " + expectedObject);
                }
            } else if (!actualObject.equals(expectedObject)) {
                Assert.fail(expected(message) + " object of " + expectedObject + " but got " + actualObject);
            }
        }
    }

    /**
     * Check that the specified field contains the expected object (as represented by the specifed
     * view. If it does not contain the expected object the test fails.
     * 
     * @group assert
     */
    public void assertFieldContains(String fieldName, TestObject expectedView) {
        assertFieldContains(null, fieldName, expectedView);
    }

    public void assertFieldContainsType(String fieldName, String expectedType) {
        assertFieldContainsType(null, fieldName, expectedType);
    }

    public void assertFieldContainsType(String message, String fieldName, String expectedType) {
        Field field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
         
        TestNaked actual = getField(fieldName);
        String actualType = ((Naked) actual.getForObject()).getShortClassName();

        if (!actualType.equals(expectedType)) {
            Assert.fail(expected(message) + " type " + expectedType + " but got " + actualType);
        }
    }

    public void assertFieldContainsType(String message, String fieldName, String title, String expectedType) {
        Naked object = getField(fieldName).getForObject();

        if (object instanceof InternalCollection) {
            InternalCollection collection = (InternalCollection) object;
            for (int i = 0; i < collection.size(); i++) {
                NakedObject element = collection.elementAt(i);
                if (element.title().toString().equals(title)) {
                    if (!element.getShortClassName().equals(expectedType)) {
                        Assert.fail(expected(message) + " object " + title + " to be of type " + expectedType + " but was "
                                + element.getShortClassName());
                    }
                    return;
                }
            }
            Assert.fail(expected(message) + " object " + title + " but could not find it in the internal collection");
        }
    }

    public void assertFieldExists(String fieldName) {
        try {
            ((NakedObject) getForObject()).getNakedClass().getField(fieldName);
        } catch (NakedClassException e) {
            throw new NakedAssertionFailedError("No field called " + fieldName + " in " + getForObject().getClass().getName());
        } 
    
    }

    public void assertActionExists(String name) {
        if (getAction(name) == null) { 
            throw new NakedAssertionFailedError("Field " + name + " is not found in " + getForObject()); }
    }

    public void assertActionExists(String name, TestObject parameter) {
        if (getAction(name, parameter) == null) { 
            throw new NakedAssertionFailedError("Field " + name + " is not found in " + getForObject()); }
    }


    public void assertFieldInvisible(String fieldName) {
        Field field = fieldFor(fieldName);
        boolean canAccess = field.canAccess(Session.getSession().getSecurityContext(), (NakedObject) getForObject());
        assertFalse("Field " + fieldName + " is visible", canAccess);
    }

    /**
     * Check that a field exists with the specified name, and it is read-only. If it does not exist,
     * or is writable, the test fails.
     * 
     * @deprecated
     */
    public void assertFieldReadOnly(String fieldName) {
        assertFieldUnmodifiable(fieldName);
    }

    public void assertNotEmpty(String fieldName) {
        assertNotEmpty(null, fieldName);
    }

    public void assertNotEmpty(String message, String fieldName) {
        Field field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
         
        TestNaked actual = getField(fieldName);
        Object object = actual.getForObject();

        // TODO refactor to remove redundancy
        if (object instanceof NakedValue) {
            if (((NakedValue) object).isEmpty()) {
                Assert.fail(expected(message) + " " + fieldName + " to contain something but it was empty");
            }
        } else {
            if (object == null) {
                Assert.fail(expected(message) + " " + fieldName + " to contain something but it was empty");
            }
        }
    }

    /**
     * Check that the title of this object is the same as the expected title. If it is not the same
     * the test fails.
     * 
     * @group assert
     */
    public void assertTitleEquals(String expectedTitle) {
        assertTitleEquals(null, expectedTitle);
    }

    /**
     * Check that the title of this object is the same as the expected title. If it is not the same
     * the test fails.
     * 
     * @param message
     *                   text to add to the failure message, which is displayed after a failure.
     * @group assert
     */
    public void assertTitleEquals(String message, String expectedTitle) {
        if (!getTitle().equals(expectedTitle)) {
            Assert.fail(expected(message) + " title of " + getForObject() + " as '" + expectedTitle + "' but got '" + getTitle()
                    + "'");
        }
    }

    public void assertType(String expectedType) {
        assertType("", expectedType);
    }

    public void assertType(String message, String expectedType) {
        String actualType = getForObject().getShortClassName();

        if (!actualType.equals(expectedType)) {
            Assert.fail(expected(message) + " type " + expectedType + " but got " + actualType);
        }    
    }

    /**
     * Drop the specified view (object) into the specified field.
     * 
     * <p>
     * If the field already contains an object then, as an object cannot be dropped on a non-empty
     * field, the test will fail.
     * </p>
     * 
     * @group action
     */
    public void associate(String fieldName, TestObject object) {
        Field field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
        assertFieldModifiable(fieldName, field);
         
        TestNaked targetField = getField(fieldName);

        if (targetField instanceof TestValue) { throw new IllegalActionError(
                "drop(..) not allowed on value target field; use fieldEntry(..) instead"); }

        if ((targetField.getForObject() != null) && !(targetField.getForObject() instanceof InternalCollection)) { throw new IllegalActionError(
                "Field already contains an object: " + targetField.getForObject()); }

        Association association = (Association) fieldFor(fieldName);
        NakedObject obj = (NakedObject) object.getForObject();

        if (association.getType() != null && !association.getType().isAssignableFrom(obj.getClass())) { throw new IllegalActionError(
                "Can't drop a " + object.getForObject().getShortClassName() + " on to the " + fieldName + " field (which accepts "
                        + association.getType() + ")"); }

        association.setAssociation((NakedObject) getForObject(), obj);
    }

    /**
     * Removes an existing object reference from the specified field. Mirrors the 'Remove Reference'
     * menu option that each object field offers by default.
     * 
     * @group action
     */
    public void clearAssociation(String fieldName) {
        Field field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
        assertFieldModifiable(fieldName, field);
         
       TestObject targetField = (TestObject) fields.get(fieldName);

        if (targetField instanceof TestValue) {
            throw new IllegalActionError("set(..) not allowed on value target field; use fieldEntry(..) instead");
        } else {
            NakedObject ref = (NakedObject) fieldFor(fieldName).get((NakedObject) getForObject());
            if(ref != null) {
                ((OneToOneAssociation) fieldFor(fieldName)).clearAssociation((NakedObject) getForObject(), ref);
            }
        }
    }

    /**
     * Removes the existing object reference, which has the specified title, from the specified
     * multi-object field. Mirrors the 'Remove Reference' menu option that each collection field
     * offers by default.
     * 
     * @group action
     */
    public void clearAssociation(String fieldName, String title) {
        Field field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
        assertFieldModifiable(fieldName, field);
         
        TestObject viewToRemove = getField(fieldName, title);
        Field assoc = fieldFor(fieldName);

        if (!(assoc instanceof OneToManyAssociation)) { throw new IllegalActionError(
                "removeReference not allowed on target field " + fieldName); }

        Naked no = viewToRemove.getForObject();

        if (!(no instanceof NakedObject)) {
            Assert.fail("A NakedObject was to be removed from the InternalCollection, but found " + no);
        }

        ((OneToManyAssociation) assoc).clearAssociation((NakedObject) getForObject(), (NakedObject) no);
    }

    private String expected(String text) {
        return ((text == null) ? "E" : text + "; e") + "xpected";
    }

    /**
     * Enters text into an editable field. Data entered here is parsed in exactly the same way as in
     * GUI, and should therefore be given in the correct form and formatted correctly. For fields
     * that normally use some other interaction, e.g. a checkbox, then the correct textual form must
     * be used (for the checkbox this is 'TRUE' and 'FALSE').
     */
    public void fieldEntry(String fieldName, String value) {
        Field field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
        assertFieldModifiable(fieldName, field);
         
        TestValue testField;
        Object view = fields.get(fieldName);

        if (view instanceof TestValue) {
            testField = (TestValue) view;
            Naked valueObject = (Naked) fieldFor(fieldName).get((NakedObject) getForObject());
            if(valueObject == null) {
                throw new NakedAssertionFailedError("Field "+ fieldName + " contains null, but should contain an NakedValue object");
            }
            testField.setForObject(valueObject);
            testField.fieldEntry(value);
            ((NakedObject) getForObject()).objectChanged();
        } else {
            throw new IllegalActionError("Can only make an entry (eg by keyboard) into a value field");
        }
    }

    private Field fieldFor(String fieldName) {
        Field att = (Field) ((NakedObject) getForObject()).getNakedClass().getField(fieldName);
        if (att == null) {
            throw new NakedAssertionFailedError("No field called " + fieldName + " in " + getForObject().getClass().getName());
        } else {
            return att;
        }
    }

    public Action getAction(String name) {
        Action action = null;

        try {
            NakedClass nakedClass = ((NakedObject) getForObject()).getNakedClass();
            action = nakedClass.getObjectAction(Action.USER, name);
        } catch (NakedClassException e) {
            Assert.fail(e.getMessage());
        }
        if(action == null) {
            Assert.fail("Method not found: " + name);
        }
        return action;
    }

    /**
     * Returns the view, from within this collection, that has the specified title.
     */
    public final TestObject getAssociation(String title) {
        if (!(getForObject() instanceof NakedCollection)) { throw new IllegalActionError(
                "selectByTitle will only select from a collection!"); }

        NakedCollection collection = (NakedCollection) getForObject();

        Enumeration e = collection.elements();
        NakedObject object = null;
        int noElements = 0;

        while (e.hasMoreElements()) {
            NakedObject element = (NakedObject) e.nextElement();

            if (element.title().toString().indexOf(title) >= 0) {
                object = element;
                noElements++;
            }
        }

        if (noElements == 0) { throw new IllegalActionError("selectByTitle must find an object within " + collection); }

        if (noElements > 1) { throw new IllegalActionError("selectByTitle must select only one object within " + collection); }

        return factory().createTestObject(context, object);
    }

    public TestNaked getField(String fieldName) {
        Field field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
         
        TestNaked view = (TestNaked) fields.get(fieldName);

        view.setForObject((Naked) fieldFor(fieldName).get((NakedObject) getForObject()));

        return view;
    }

    /**
     * Get the view for the object held within the named collection view, that has the specified
     * title.
     */
    public TestObject getField(String fieldName, String title) {
        Field field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
                 
        TestObject view = (TestObject) fields.get(fieldName);

        view.setForObject((Naked) fieldFor(fieldName).get((NakedObject) getForObject()));

        if (!(view.getForObject() instanceof InternalCollection)) { throw new IllegalActionError(
                "getField(String, String) only allows an object to be selected from an InternalCollection"); }

        Enumeration e = ((NakedCollection) view.getForObject()).elements();
        NakedObject object = null;
        int noElements = 0;

        while (e.hasMoreElements()) {
            NakedObject element = (NakedObject) e.nextElement();

            if (element.title().toString().equals(title)) {
                object = element;
                noElements++;
            }
        }

        if (noElements == 0) { throw new IllegalActionError("The field " + fieldName + " must contain an object titled " + title
                + " within it"); }

        if (noElements > 1) { throw new IllegalActionError("The field " + fieldName + " must only contain one object titled "
                + title + " within it"); }

        return factory().createTestObject(context, object);
    }

    /**
     * returns the title of the object as a String
     */
    public String getFieldTitle(String fieldName) {
        Field field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
         
        if (getField(fieldName).getForObject() == null) { throw new IllegalActionError("No object to get title from in field "
                + fieldName + " within " + getForObject()); }

        return getField(fieldName).getTitle();
    }

    /**
     * returns the title of the object as a String
     */
    public String getTitle() {
        if (getForObject() == null) { throw new IllegalActionError("??"); }

        return getForObject().title().toString();
    }

    /**
     * Invokes this object's zero-parameter action method of the the given name. This mimicks the
     * right-clicking on an object and subsequent selection of a menu item.
     */
    public TestObject invokeAction(String name) {
        Action action = getAction(name);
        assertActionUsable(name, action);
        assertActionVisible(name, action);

        NakedObject result = action.execute((NakedObject) getForObject());	
        return ((result == null) ? null : factory().createTestObject(context, result));
    }

    /**
     * Drop the specified view (object) onto this object and invoke the corresponding
     * <code>action</code> method. A new view representing the returned object, if any is
     * returned, from the invoked <code>action</code> method is returned by this method.
     */
    public TestObject invokeAction(String name, TestObject parameter) {
        Action action = getAction(name, parameter);

        NakedObject dropObject = (NakedObject) parameter.getForObject();
        boolean allowed = action.getAbout(context, (NakedObject) getForObject(), dropObject).canUse().isAllowed();
        assertTrue("action " + name + " is unusable", allowed);

        allowed = action.getAbout(context, (NakedObject) getForObject(), dropObject).canAccess().isAllowed();
        assertTrue("action " + name + " is invisible", allowed);
 
        NakedObject result = action.execute((NakedObject) getForObject(), dropObject);
        if (result == null) {
            return null;
        } else {
            return factory().createTestObject(context, result);
        }
    }

    /**
     * Test the named field by calling fieldEntry with the specifed value and then check the value
     * stored is the same.
     */
    public void testField(String fieldName, String expectedValue) {
        testField(fieldName, expectedValue, expectedValue);
    }

    /**
     * Test the named field by calling fieldEntry with the set value and then check the value stored
     * against the expected value.
     */
    public void testField(String fieldName, String setValue, String expectedValue) {
        fieldEntry(fieldName, setValue);
        Assert.assertEquals("Field " + fieldName + "contains unexpected value", expectedValue, getField(fieldName).getTitle());
    }

    /**
     * Test the named field by calling fieldEntry with the set value and then check the value stored
     * against the expected value.
     */
    public void testField(String fieldName, TestObject expected) {
        associate(fieldName, expected);
        Assert.assertEquals(expected.getForObject(), getField(fieldName).getForObject());
    }

    public String toString() {
        return getForObject().toString();
    }

    
    
    public void assertActionUnusable(String name) {
        assertActionUnusable(name, getAction(name));
    }
    
    private void assertActionUnusable(String name, Action action) {
        boolean vetoed = action.getAbout(context, (NakedObject) getForObject()).canUse().isVetoed();
        Assert.assertTrue("action " + name + " is usable", vetoed);
    }

    public void assertActionVisible(String name) {
        assertActionVisible(name, getAction(name));
    }

    private void assertActionVisible(String name, Action action) {
        boolean allowed = action.getAbout(context, (NakedObject) getForObject()).canAccess().isAllowed();
        assertTrue("action " + name + " is invisible", allowed);
    }

    public void assertActionInvisible(String name) {
        assertActionInvisible(name, getAction(name));
    }

    private void assertActionInvisible(String name, Action action) {
        boolean vetoed = action.getAbout(context, (NakedObject) getForObject()).canAccess().isVetoed();
        Assert.assertTrue("action " + name + " is visible", vetoed);
    }

    public void assertActionVisible(String name, TestObject parameter) {
        assertActionVisible(name, getAction(name, parameter));
    }

    public void assertActionInvisible(String name, TestObject parameter) {
        assertActionInvisible(name, getAction(name, parameter));
    }

    public void assertActionUsable(String name) {
        assertActionUsable(name, getAction(name));
    }

    private void assertActionUsable(String name, Action action) {
        boolean allowed = action.getAbout(context, (NakedObject) getForObject()).canUse().isAllowed();
        assertTrue("action " + name + " is unusable", allowed);
    }

    public void assertActionUnusable(String name, TestObject parameter) {
       assertActionUnusable(name, getAction(name, parameter));
    }

    public Action getAction(String name, TestObject parameter) {
        NakedObject parameterObject = (NakedObject) parameter.getForObject();
        try {
            NakedClass nakedClass = ((NakedObject) getForObject()).getNakedClass();
            NakedClass[] parameters = new NakedClass[] { parameterObject.getNakedClass() };
            Action action = nakedClass.getObjectAction(Action.USER, name, parameters);
            if(action == null) {
                Assert.fail("Method not found: " + name + "(" + parameter + ")");
            }
            return action;
        } catch (NakedClassException e) {
            String parameterName = parameterObject.getShortClassName();
            String targetName = getForObject().getShortClassName();
            Assert.fail("Can't find action " + name + " with " + parameterName + " on a "
                    + targetName);
            return null;
        }
    }

    public void assertActionUsable(String name, TestObject parameter) {
        assertActionUsable(name, getAction(name, parameter));
    }

    public void assertFieldVisible(String fieldName) {
        Field field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
    }
    
    private void assertFieldVisible(String fieldName, Field field) {
        boolean canAccess = field.canAccess(Session.getSession().getSecurityContext(), (NakedObject) getForObject());
        assertTrue("Field " + fieldName + " is invisible", canAccess);    
    }

    public void assertFieldModifiable(String fieldName) {
        Field field = fieldFor(fieldName);
        assertFieldModifiable(fieldName, field);
    }
    
    private void assertFieldModifiable(String fieldName, Field field) {
        SecurityContext securityContext = Session.getSession().getSecurityContext();
        boolean canAccess = field.canUse(securityContext, (NakedObject) getForObject());
        assertTrue("Field " + fieldName + " is unmodifiable for " + securityContext.getUser(), canAccess);
    }
   
    private void assertFalse(String message, boolean condition) {
        if(condition) {
            throw new NakedAssertionFailedError(message);
        }
    }
    
    private void assertTrue(String message, boolean condition) {
        if(! condition) {
            throw new NakedAssertionFailedError(message);
        }
    }

    public void assertFieldUnmodifiable(String fieldName) {      
        Field field = fieldFor(fieldName);
        boolean canAccess = field.canUse(Session.getSession().getSecurityContext(), (NakedObject) getForObject());
        assertFalse("Field " + fieldName + " is modifiable", canAccess);
    }
}