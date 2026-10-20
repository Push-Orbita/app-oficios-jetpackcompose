# Backend — El soft delete de usuario no libera el DNI

## Resumen para el equipo de backend

`DELETE /usuario/{id}` (soft delete) da de baja la cuenta correctamente — el usuario ya no puede loguearse — pero el **documento (DNI) queda bloqueado para siempre**, incluso para una cuenta completamente nueva con otro email. Cualquier usuario que borre su cuenta (o alguien la borre por él) pierde la posibilidad de volver a registrarse con su propio DNI.

## Evidencia — reproducido con datos reales

### 1. La cuenta está efectivamente borrada

```
POST /auth/login
{"email":"<EMAIL_DE_PRUEBA>","contrasena":"<CONTRASENA_DE_PRUEBA>"}

→ 404 Not Found
{
  "error": true,
  "errorDetails": {
    "code": "DB03",
    "message": "Registro no encontrado en la base de datos.",
    "details": "{\"email\":\"<EMAIL_DE_PRUEBA>\"}"
  }
}
```

### 2. El DNI de esa cuenta sigue bloqueado para un registro 100% nuevo

Se intentó crear una cuenta completamente distinta (email nunca usado antes) reutilizando el mismo DNI:

```
POST /auth/signup (multipart/form-data)
nombres=Test, apellido=Reproduccion, email=<EMAIL_NUEVO_DE_PRUEBA>,
contrasena=<CONTRASENA_DE_PRUEBA>, rolId=2, documento=<DNI_DE_PRUEBA>

→ 409 Conflict
{
  "error": true,
  "errorDetails": {
    "code": "US04",
    "message": "El documento ya está registrado. No podés volver a registrarte con el mismo DNI.",
    "details": "{\"documento\":\"<DNI_DE_PRUEBA>\"}"
  }
}
```

## Qué confirma esto

- La baja de usuario (`DELETE /usuario/{id}`) es un soft delete: el registro deja de ser accesible por login, pero la fila (o al menos el campo `documento`) sigue existiendo en la tabla y participando en la validación de unicidad.
- No es un problema de la cuenta específica — se reprodujo con un **email nuevo, nunca antes registrado**, así que descarta cualquier explicación relacionada al email.

## Qué pedimos revisar

1. Que la validación de "documento ya registrado" (`US04`) **excluya los usuarios soft-deleted** (filtro por `activo`/`deletedAt IS NULL` o como esté modelado), tanto en el chequeo de duplicados en `/auth/signup` como en cualquier otro lugar que valide DNI.
2. Si el soft delete es el comportamiento definitivo (no hay borrado físico), confirmarlo explícitamente — así sabemos que la única vía para "liberar" un DNI es que el propio backend lo desvincule, y lo documentamos como comportamiento esperado del lado de la app (ej. mensaje al usuario: "contactate con soporte" en vez de simplemente reintentar).

## Impacto actual

Cualquier cuenta dada de baja (por el usuario o por administración) deja su DNI inutilizable para siempre en la plataforma — ni el mismo usuario ni nadie más puede volver a registrarse con ese documento. Esto es especialmente grave si en producción los usuarios tienen la opción de "darse de baja" desde la app (funcionalidad prevista en el perfil, Fase 5 del roadmap).
