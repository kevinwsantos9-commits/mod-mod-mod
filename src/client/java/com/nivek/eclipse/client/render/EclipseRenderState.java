package com.nivek.eclipse.client.render;

/** Dados extras do PlayerRenderState (implementada via mixin). */
public interface EclipseRenderState {
    int nivekeclipse$getMutation();

    void nivekeclipse$setMutation(int mutation);

    /** Mascara de bits dos implantes instalados (ver ImplantSlot). */
    int nivekeclipse$getImplants();

    void nivekeclipse$setImplants(int mask);

    /** Indice da forma de transformacao ativa (ver Form); -1 = nenhuma. */
    int nivekeclipse$getForm();

    void nivekeclipse$setForm(int form);

    /** 0..1 durante a animacao de transformacao; 1 = concluida. */
    float nivekeclipse$getTransform();

    void nivekeclipse$setTransform(float transform);
}
