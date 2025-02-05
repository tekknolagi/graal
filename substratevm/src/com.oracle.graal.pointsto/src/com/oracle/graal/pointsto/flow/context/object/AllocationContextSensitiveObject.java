/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.graal.pointsto.flow.context.object;

import com.oracle.graal.pointsto.PointsToAnalysis;
import com.oracle.graal.pointsto.flow.ArrayElementsTypeFlow;
import com.oracle.graal.pointsto.flow.context.AnalysisContext;
import com.oracle.graal.pointsto.meta.AnalysisType;
import com.oracle.graal.pointsto.typestate.TypeState;

import jdk.vm.ci.code.BytecodePosition;

/**
 * Abstraction of a allocation context sensitive heap allocated object.
 */
public class AllocationContextSensitiveObject extends ContextSensitiveAnalysisObject {

    /** Method and bytecode index of allocation site. */
    protected final BytecodePosition allocationLabel;
    /** The context of the heap object. */
    protected final AnalysisContext allocationContext;
    /** The context of the method allocating this object. */
    protected AnalysisContext allocatorContext;

    /**
     * Creates the allocation sensitive object corresponding to a clone for an allocation site. It
     * has the same type and allocation site information as the original heap object, but a specific
     * context information.
     */
    public AllocationContextSensitiveObject(PointsToAnalysis bb, AnalysisType type, BytecodePosition allocationSite, AnalysisContext context) {
        super(bb.getUniverse(), type, AnalysisObjectKind.AllocationContextSensitive);
        assert bb.trackConcreteAnalysisObjects(type);
        this.allocationLabel = allocationSite;
        this.allocationContext = context;
        assert allocationSite != null;
    }

    public BytecodePosition allocationLabel() {
        return this.allocationLabel;
    }

    public AnalysisContext allocationContext() {
        return allocationContext;
    }

    @Override
    public ArrayElementsTypeFlow getArrayElementsFlow(PointsToAnalysis bb, boolean isStore) {
        assert type.isArray();
        assert bb.analysisPolicy().allocationSiteSensitiveHeap();

        if (!arrayElementsTypeStore.writeFlow().getState().canBeNull()) {
            /*
             * Initialize the elements flow of all heap allocated arrays with the null type state.
             */
            /* The constant array elements flows are initialized in BigBang.scan() ? */
            arrayElementsTypeStore.writeFlow().addState(bb, TypeState.forNull());
        }

        return isStore ? arrayElementsTypeStore.writeFlow() : arrayElementsTypeStore.readFlow();
    }

    @Override
    public String toString() {
        return super.toString() + "  " + allocationLabel + "  " + allocationContext;
    }

    /*
     * The identity of heap objects is based on the id. We don't want to use the actual value of a
     * field object for this purpose (i.e., the allocationLabel, heapContext and type) because
     * various heap objects generated by the same clone type flow have the same value but are not
     * identical.
     */

}
