package com.ggetters.app.data.remote.mappers

import com.ggetters.app.data.local.entities.UserEntity
import com.ggetters.app.data.remote.model.UserDto
import com.google.firebase.Timestamp

fun UserDto.toEntity(): UserEntity = UserEntity(
    id = id,
    teamId = teamId,
    authId = authId,
    code = code,
    name = name,
    surname = surname,
    alias = alias,
    role = role,
    gender = gender,
    dateOfBirth = dateOfBirth.toDate(),
    annexedAt = annexedAt?.toDate(),
    createdAt = createdAt.toDate(),
    updatedAt = updatedAt.toDate(),
    stampedAt = stampedAt?.toDate()
)

fun UserEntity.toDto(): UserDto = UserDto(
    id = id,
    teamId = teamId,
    authId = authId,
    code = code,
    name = name,
    surname = surname,
    alias = alias,
    role = role,
    gender = gender,
    dateOfBirth = Timestamp(dateOfBirth),
    annexedAt = annexedAt?.let { Timestamp(it) },
    createdAt = Timestamp(createdAt),
    updatedAt = Timestamp(updatedAt),
    stampedAt = stampedAt?.let { Timestamp(it) }
)