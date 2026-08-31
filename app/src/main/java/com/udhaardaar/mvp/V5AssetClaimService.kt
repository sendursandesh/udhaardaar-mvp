package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject

class V5AssetClaimService(context: Context) {
    private val store = V5LocalStore(context)

    fun addFinancialAsset(asset: V5AssetVaultModels.FinancialAsset) = store.replace("financial_assets", JSONObject().apply {
        put("id",asset.id);put("ownerId",asset.ownerId);put("category",asset.category);put("institution",asset.institution);put("reference",asset.maskedReference);put("value",asset.value ?: JSONObject.NULL);put("maturity",asset.maturityDate ?: "");put("nomineeId",asset.nomineeId ?: "");put("documents",asset.documentIds.joinToString(","))
    })

    fun addNonFinancialAsset(asset: V5AssetVaultModels.NonFinancialAsset) = store.replace("non_financial_assets", JSONObject().apply {
        put("id",asset.id);put("ownerId",asset.ownerId);put("category",asset.category);put("title",asset.title);put("location",asset.location ?: "");put("ownershipReference",asset.ownershipReference ?: "");put("value",asset.value ?: JSONObject.NULL);put("nomineeId",asset.nomineeId ?: "");put("documents",asset.documentIds.joinToString(","))
    })

    fun openClaim(claim: V5AssetVaultModels.AssetClaim) = store.replace("claims", JSONObject().apply {
        put("id",claim.id);put("assetId",claim.assetId);put("claimantId",claim.claimantId);put("relationship",claim.relationship);put("status",claim.status.name);put("institution",claim.institution);put("required",claim.requiredDocuments.joinToString(","));put("supplied",claim.suppliedDocuments.joinToString(","));put("nextAction",claim.nextAction ?: "")
    })
}
