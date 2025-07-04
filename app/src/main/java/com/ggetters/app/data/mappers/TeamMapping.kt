//package com.ggetters.app.data.mappers
//
//import com.ggetters.app.data.local.entities.TeamEntity
//import com.ggetters.app.data.remote.model.TeamDto
//import com.google.firebase.Timestamp
//
//fun TeamDto.toEntity(): TeamEntity = TeamEntity(
//    id = id,
//    code = code,
//    createdAt = createdAt.toDate(),
//    updatedAt = updatedAt.toDate(),
//    stashedAt = stashedAt?.toDate()
//)
//
//fun TeamEntity.toDto(): TeamDto = TeamDto(
//    id = id,
//    code = code,
//    createdAt = Timestamp(createdAt),
//    updatedAt = Timestamp(updatedAt),
//    stashedAt = stashedAt?.let { Timestamp(it) }
//)